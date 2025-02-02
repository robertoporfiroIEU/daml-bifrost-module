package co.topl.daml.algebras

import java.io.File

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.io.Source

import cats.arrow.FunctionK
import cats.data.EitherT
import cats.data.NonEmptyChain
import cats.effect.IO
import co.topl.akkahttprpc.implicits.client._
import co.topl.attestation.AddressCodec.implicits._
import co.topl.attestation.PublicKeyPropositionCurve25519._
import co.topl.attestation._
import co.topl.attestation.keyManagement.KeyRing
import co.topl.attestation.keyManagement.KeyfileCurve25519
import co.topl.attestation.keyManagement.KeyfileCurve25519Companion
import co.topl.attestation.keyManagement.PrivateKeyCurve25519
import co.topl.daml.RpcClientFailureException
import co.topl.daml.api.model.topl.asset.AssetTransferRequest
import co.topl.daml.api.model.topl.transfer.TransferRequest
import co.topl.modifier.box.AssetCode
import co.topl.modifier.box.AssetValue
import co.topl.modifier.box.SecurityRoot
import co.topl.modifier.box.SimpleValue
import co.topl.modifier.box.TokenValueHolder
import co.topl.modifier.transaction.PolyTransfer
import co.topl.modifier.transaction.builder.BoxSelectionAlgorithms
import co.topl.modifier.transaction.serialization.AssetTransferSerializer
import co.topl.modifier.transaction.serialization.PolyTransferSerializer
import co.topl.rpc.ToplRpc
import co.topl.rpc.ToplRpc.Transaction.RawPolyTransfer
import co.topl.rpc.implicits.client._
import co.topl.rpc.implicits.client._
import co.topl.utils.IdiomaticScalaTransition.implicits.toValidatedOps
import co.topl.utils.Int128
import co.topl.utils.StringDataTypes
import co.topl.utils.StringDataTypes.Base58Data
import io.circe.Json
import scodec.bits.ByteVector

trait PolySpecificOperationsAlgebra
    extends AssetSpecificOperationsAlgebra[PolyTransfer, IO]
    with CommonBlockchainOpsAlgebraImpl {

  import toplContext.provider._

  def parseTxM(msg2Sign: Array[Byte]) = IO.fromEither {
    import io.circe.parser._
    import co.topl.modifier.transaction.PolyTransfer.jsonDecoder
    parse(new String(msg2Sign)).flatMap(jsonDecoder.decodeJson)
  }

  def signTxM(rawTx: PolyTransfer[_ <: Proposition]) = IO {
    val signFunc = (addr: Address) => keyRing.generateAttestation(addr)(rawTx.messageToSign)
    val signatures = keyRing.addresses.map(signFunc).reduce(_ ++ _)
    rawTx.copy(attestation = signatures)
  }

  def broadcastTransactionM(
    signedTx: PolyTransfer[_ <: Proposition]
  ) = for {
    eitherBroadcast <- IO.fromFuture(
      IO(
        ToplRpc.Transaction.BroadcastTx
          .rpc(ToplRpc.Transaction.BroadcastTx.Params(signedTx))
          .leftMap(failure =>
            RpcClientFailureException(
              failure
            )
          )
          .value
      )
    )
    broadcast <- IO.fromEither(eitherBroadcast)
  } yield broadcast

  def deserializeTransactionM(transactionAsBytes: Array[Byte]): IO[PolyTransfer[_ <: Proposition]] = IO.fromEither {
    import io.circe.parser._
    import co.topl.modifier.transaction.PolyTransfer.jsonDecoder
    parse(new String(transactionAsBytes)).flatMap(jsonDecoder.decodeJson)
  }

  def encodeTransferEd25519M(assetTransfer: PolyTransfer[PublicKeyPropositionEd25519]): IO[String] = for {
    transferRequest <- IO {
      import io.circe.syntax._
      assetTransfer.asJson.noSpaces
    }
  } yield transferRequest

  def encodeTransferM(assetTransfer: PolyTransfer[PublicKeyPropositionCurve25519]): IO[String] = for {
    transferRequest <- IO {
      import io.circe.syntax._
      assetTransfer.asJson.noSpaces
    }
  } yield transferRequest

  def createParamsM(transferRequest: TransferRequest): IO[RawPolyTransfer.Params] =
    IO(
      RawPolyTransfer.Params(
        propositionType =
          PublicKeyPropositionCurve25519.typeString, // required fixed string for now, exciting possibilities in the future!
        sender = NonEmptyChain
          .fromSeq(
            transferRequest.from.asScala.toSeq
              .map(Base58Data.unsafe)
              .map(_.decodeAddress.getOrThrow())
          )
          .get, // Set of addresses whose state you want to use for the transaction
        recipients = NonEmptyChain
          .fromSeq(
            transferRequest.to.asScala.toSeq.map(x =>
              (
                Base58Data.unsafe(x._1).decodeAddress.getOrThrow(),
                Int128(x._2.intValue())
              )
            )
          )
          .get, // Chain of (Recipients, Value) tuples that represent the output boxes
        fee = Int128(
          transferRequest.fee
        ), // fee to be paid to the network for the transaction (unit is nanoPoly)
        changeAddress = Base58Data
          .unsafe(transferRequest.changeAddress)
          .decodeAddress
          .getOrThrow(), // who will get ALL the change from the transaction?
        data = None, // upto 128 Latin-1 encoded characters of optional data,
        boxSelectionAlgorithm = BoxSelectionAlgorithms.All
      )
    )

  def createRawTxM(params: RawPolyTransfer.Params) =
    for {
      eitherTx <- IO.fromFuture(
        IO(
          ToplRpc.Transaction.RawPolyTransfer
            .rpc(params)
            .value
        )
      )
      rawTx <- IO.fromEither(eitherTx.left.map(x => RpcClientFailureException(x)))
    } yield rawTx.rawTx

}
