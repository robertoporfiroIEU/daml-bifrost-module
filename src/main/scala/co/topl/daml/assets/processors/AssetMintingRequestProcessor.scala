package co.topl.daml.assets.processors

import java.util.concurrent.TimeoutException
import java.util.stream

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.concurrent.Await
import scala.concurrent.duration._

import cats.data.EitherT
import cats.data.NonEmptyChain
import cats.effect.IO
import cats.syntax.traverse._
import co.topl.akkahttprpc.implicits.client.rpcToClient
import co.topl.attestation.AddressCodec.implicits._
import co.topl.attestation.PublicKeyPropositionCurve25519._
import co.topl.attestation._
import co.topl.daml.AbstractProcessor
import co.topl.daml.DamlAppContext
import co.topl.daml.RpcClientFailureException
import co.topl.daml.ToplContext
import co.topl.daml.algebras.AssetOperationsAlgebra
import co.topl.daml.api.model.topl.asset.AssetMintingRequest
import co.topl.modifier.box.AssetCode
import co.topl.modifier.box.AssetValue
import co.topl.modifier.box.SecurityRoot
import co.topl.modifier.box.SimpleValue
import co.topl.modifier.box.TokenValueHolder
import co.topl.modifier.transaction.AssetTransfer
import co.topl.modifier.transaction.builder.BoxSelectionAlgorithms
import co.topl.modifier.transaction.serialization.AssetTransferSerializer
import co.topl.rpc.ToplRpc
import co.topl.rpc.implicits.client._
import co.topl.utils.IdiomaticScalaTransition.implicits.toValidatedOps
import co.topl.utils.Int128
import co.topl.utils.StringDataTypes
import co.topl.utils.StringDataTypes.Base58Data
import co.topl.utils.StringDataTypes.Latin1Data
import com.daml.ledger.javaapi.data.Command
import com.daml.ledger.javaapi.data.CreatedEvent
import org.slf4j.LoggerFactory
import scodec.bits.ByteVector

import ToplRpc.Transaction.RawAssetTransfer

/**
 * This processor processes the minting requests.
 *
 * @param damlAppContext the context of the DAML application
 * @param toplContext the context for Topl blockain, in particular the provider
 * @param timeoutMillis the timeout before processing fails
 * @param callback a function that performs operations before the processing is done. Its result is returned by the processor when there are no errors.
 * @param onError a function executed when there is an error sending the commands to the DAML server. Its result is returned by the processor when there are errors in the DAML.
 */
class AssetMintingRequestProcessor(
  damlAppContext: DamlAppContext,
  toplContext:    ToplContext,
  timeoutMillis:  Int,
  callback:       java.util.function.BiFunction[AssetMintingRequest, AssetMintingRequest.ContractId, Boolean],
  onError:        java.util.function.Function[Throwable, Boolean]
) extends AbstractProcessor(damlAppContext, toplContext, callback, onError)
    with AssetOperationsAlgebra {

  def this(
    damlAppContext: DamlAppContext,
    toplContext:    ToplContext
  ) =
    this(damlAppContext, toplContext, 3000, (x, y) => true, x => true)

  import toplContext.provider._

  implicit val ev = assetMintingRequestEv

  def processMintingRequestM(
    assetMintingRequest:    AssetMintingRequest,
    mintingRequestContract: AssetMintingRequest.ContractId
  ) =
    (for {
      address       <- decodeAddressesM(assetMintingRequest.from.asScala.toList)
      changeAddress <- decodeAddressM(assetMintingRequest.changeAddress)
      params        <- getParamsM(address)
      balance       <- getBalanceM(params)
      value         <- computeValueM(assetMintingRequest.fee, balance)
      tailList = assetMintingRequest.to.asScala.toList.map(t => (createToParamM(assetMintingRequest) _)(t._1, t._2))
      listOfToAddresses <- (IO((changeAddress, value)) :: tailList).sequence
      assetTransfer <- createAssetTransferM(
        assetMintingRequest.fee,
        None,
        address,
        balance,
        listOfToAddresses
      )
      encodedTx <- encodeTransferM(assetTransfer)
    } yield {
      logger.info("Successfully generated raw transaction for contract {}.", mintingRequestContract)
      import io.circe.syntax._
      logger.info("The returned json: {}", assetTransfer.asJson)
      logger.info(
        "Encoded transaction: {}",
        encodedTx
      )

      stream.Stream.of(
        mintingRequestContract
          .exerciseMintingRequest_Accept(
            encodedTx,
            assetTransfer.newBoxes.toList.reverse.head.nonce
          )
      ): stream.Stream[Command]
    }).handleError { failure =>
      logger.info("Failed to build transaction.")
      logger.debug("Error: {}", failure)

      stream.Stream.of(
        mintingRequestContract
          .exerciseMintingRequest_Reject()
      ): stream.Stream[Command]
    }

  def processEvent(
    workflowsId: String,
    event:       CreatedEvent
  ): IO[(Boolean, stream.Stream[Command])] =
    processEventAux(
      AssetMintingRequest.TEMPLATE_ID,
      e => AssetMintingRequest.fromValue(e.getArguments()),
      e => AssetMintingRequest.Contract.fromCreatedEvent(e).id,
      callback.apply,
      event
    )(processMintingRequestM).timeout(timeoutMillis.millis)

}
