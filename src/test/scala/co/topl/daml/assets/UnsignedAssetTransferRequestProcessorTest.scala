package co.topl.daml.assets

import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite
import co.topl.daml.api.model.topl.asset.AssetMintingRequest
import java.util.Optional
import java.{util => ju}
import co.topl.daml.api.model.da.types
import co.topl.daml.api.model.topl.utils.AssetCode
import co.topl.daml.DamlAppContext
import co.topl.daml.ToplContext
import co.topl.daml.assets.processors.AssetMintingRequestProcessor
import com.daml.ledger.javaapi.data.Command
import akka.http.scaladsl.model.Uri
import co.topl.client.Provider
import akka.actor.ActorSystem
import co.topl.rpc.ToplRpc
import co.topl.attestation.Address
import co.topl.utils.Int128
import co.topl.attestation.Evidence
import co.topl.utils.NetworkType
import co.topl.modifier.box.PolyBox
import co.topl.modifier.box.SimpleValue
import com.daml.ledger.api.v1.EventOuterClass.CreatedEvent
import com.daml.ledger.javaapi.data
import com.daml.ledger.api.v1.TransactionOuterClass.Transaction
import com.daml.ledger.api.v1.EventOuterClass.Event
import co.topl.daml.api.model.topl.asset.UnsignedAssetTransferRequest
import co.topl.daml.base.UnsignedAssetTransferRequestProcessorBaseTest

class UnsignedAssetTransferRequestProcessorTest
    extends CatsEffectSuite
    with UnsignedAssetTransferRequestProcessorBaseTest {

  test("UnsignedAssetTransferRequestProcessor should exercise UnsignedAssetTransfer_Sign") {

    dummyStandardProcessor
      .signOperationM(assetTransferRequest, assetTransferRequestContract)
      .map { x =>
        val command = x.collect(ju.stream.Collectors.toList()).get(0).asExerciseCommand().get()
        assertEquals(command.getChoice(), "UnsignedAssetTransfer_Sign")
      }
  }

  test("UnsignedAssetTransferRequestProcessor should exercise UnsignedAssetTransfer_Archive") {

    dummyFailingWithException
      .signOperationM(assetTransferRequest, assetTransferRequestContract)
      .map { x =>
        val command = x.collect(ju.stream.Collectors.toList()).get(0).asExerciseCommand().get()
        assertEquals(command.getChoice(), "UnsignedAssetTransfer_Archive")
      }
  }

  test("UnsignedAssetTransferRequestProcessor should return false if the error function returns false") {

    val event: data.Event =
      data.CreatedEvent.fromProto(
        CreatedEvent
          .newBuilder()
          .setTemplateId(UnsignedAssetTransferRequest.TEMPLATE_ID.toProto())
          .setCreateArguments(assetTransferRequest.toValue().toProto().getRecord())
          .build()
      )
    val listOfEvents = new ju.ArrayList[Event]()
    listOfEvents.add(event.toProtoEvent())
    val tx = data.Transaction.fromProto(Transaction.newBuilder().addAllEvents(listOfEvents).build())

    dummyStandardProcessorWithErrorReturningTrue
      .processTransactionIO(tx)
      .map { x =>
        assertEquals(x, false)
      }
  }

  test("UnsignedAssetTransferRequestProcessor should return false if the condition function return false") {

    val event =
      data.CreatedEvent.fromProto(
        CreatedEvent
          .newBuilder()
          .setTemplateId(UnsignedAssetTransferRequest.TEMPLATE_ID.toProto())
          .setCreateArguments(assetTransferRequest.toValue().toProto().getRecord())
          .build()
      )
    dummyFailCondition
      .processEvent("xxx", event)
      .map { x =>
        assertEquals(x._1, false)
      }
  }

}
