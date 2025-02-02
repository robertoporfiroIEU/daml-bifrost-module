### Asset Contracts

#### `AssetBalanceRequest`

The asset balance request contract models a request from some user to the operator to get the quantity of a given asset in a given address.

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested the balance.                        |
| assetCode      | AssetCode      | The code of the asset we are inquiring about                 |
| address        | Text           | The address where the assets are stored.                     |

##### Choices

##### `AssetBalanceRequest_Accept`

Accepts the request indicating that is has computed the balance at the given address. It creates
a response contract of type `AssetBalanceResponse`.

###### Parameters

| Name     | Type | Description                                                  |
| -------- | ---- | ------------------------------------------------------------ |
| quantity | Int  | The quantity of the given asset present.                     |


##### `AssetBalanceRequest_Reject`

Rejects the request.

###### Parameters

None.


#### `AssetBalanceResponse`

The asset balance response models the answer to a balance request. It has the following fields as keys:
`requestor`, `address`, `assetCode`. Since there can only be one response at a time for a given address, requestor
and asset, we provide an archive choice to allow the user to query the balance from another contract.

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested the balance.                        |
| assetCode      | AssetCode      | The code of the asset we are inquiring about                 |
| address        | Text           | The address where the assets are stored.                     |
| quantity       | Int            | The quantity of the given asset present.                     |

##### Choices

##### `AssetBalanceResponse_Archive `

Archives the request the request after the user has obtained the requred values. 

###### Parameters

None.

#### `AssetMintingRequest`

The asset minting request contract models a request from some user to the operator to mint a particular asset. The asset to mint is passed as a parameter at the creation time of the contract.

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested this minting operation.             |
| someOrgId      | Optional Text  | If this minting request is related to an organization, this field contains the organization id, otherwise, `None`. |
| from           | [ Text ]       | A list of from addresses encoded in Base58. The addresses from where the assets are going to be minted. |
| to             | [ (Text, Int)] | A list of pairs containing the addresses and the amounts to be transferred to the to addresses. |
| changeAddress  | Text           | The address where the change will be sent after the transaction. |
| assetCode      | AssetCode      | The code of the asset being minted                           |
| quantity       | Int            | The number of assets to mint.                                |
| someCommitRoot | Optional Text  | A commit root encoded in a base 58 string or `None` if there is not commit root. |
| someMetadata   | Optional Text  | Some metadata or `None` if no metadata is needed.            |
| fee            | Int            | The amount of polys that are paid to perform this transaction. |

##### Choices

##### `MintingRequest_Accept`

Accepts the request for further processing by the operator.

###### Parameters

| Name     | Type | Description                                                  |
| -------- | ---- | ------------------------------------------------------------ |
| txToSign | Text | The transaction to be signed serialized by the `AssetTransferSerializer` and encoded in JSON. |
| boxNonce | Int  | The nonce of the box where the asset is going to be stored.  |

##### `MintingRequest_Reject`

Rejects the request.

###### Parameters

None.

##### `MintingRequest_Archive`

Archives the request. Archive implies that the archiving was not done because of an error.

###### Parameters

None.

#### `UnsignedAssetMinting`

An unsigned asset minting. It is created when a `MintingRequest_Accept` is exercise in an `AssetMintingRequest`. In includes all the data of the `AssetMintingRequest` and also the transaction to sign  encoded in JSON and the box nonce. 

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested this minting operation.             |
| someOrgId      | Optional Text  | If this minting request is related to an organization, this field contains the organization id, otherwise, `None`. |
| from           | [ Text ]       | A list of from addresses encoded in Base58. The addresses from where the assets are going to be minted. |
| to             | [ (Text, Int)] | A list of pairs containing the addresses and the amounts to be transferred to the to addresses. |
| changeAddress  | Text           | The address where the change will be sent after the transaction. |
| assetCode      | AssetCode      | The code of the asset being minted                           |
| quantity       | Int            | The number of assets to mint.                                |
| someCommitRoot | Optional Text  | A commit root encoded in a base 58 string or `None` if there is not commit root. |
| someMetadata   | Optional Text  | Some metadata or `None` if no metadata is needed.            |
| boxNonce       | Int            | The nonce of the box where the asset is going to be stored.  |
| fee            | Int            | The amount of polys that are paid to perform this transaction. |
| mintTxToSign   | Text           | The minting transaction to be signed serialized by the `AssetTransferSerializer` and encoded in JSON. |

##### Choices

##### `UnsignedMinting_Sign`

Signs the request for broadcasting to the network by the operator.

###### Parameters

| Name         | Type | Description                                                  |
| ------------ | ---- | ------------------------------------------------------------ |
| signedMintTx | Text | The signed minting transaction serialized by the `AssetTransferSerializer` and encoded in JSON. |

##### `UnsignedMinting_Reject`

Rejects the signing request.

###### Parameters

None.

##### `UnsignedMinting_Archive`

Archives the signing request. Archive implies that the archiving was not done because of an error.

###### Parameters

None.

#### `SignedAssetMinting`

A signed asset minting request. It is created when a `UnsignedMinting_Sign` is exercised in an `UnsignedAssetMinting`. In includes all the data of the `UnsignedAssetMinting` and also the signed transaction encoded in JSON and a send status. The send status informs the current status of the minting request with respect to the network. 

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested this minting operation.             |
| someOrgId      | Optional Text  | If this minting request is related to an organization, this field contains the organization id, otherwise, `None`. |
| from           | [ Text ]       | A list of from addresses encoded in Base58. The addresses from where the assets are going to be minted. |
| to             | [ (Text, Int)] | A list of pairs containing the addresses and the amounts to be transferred to the to addresses. |
| changeAddress  | Text           | The address where the change will be sent after the transaction. |
| assetCode      | AssetCode      | The code of the asset being minted                           |
| quantity       | Int            | The number of assets to mint.                                |
| someCommitRoot | Optional Text  | A commit root encoded in a base 58 string or `None` if there is not commit root. |
| someMetadata   | Optional Text  | Some metadata or `None` if no metadata is needed.            |
| boxNonce       | Int            | The nonce of the box where the asset is going to be stored.  |
| fee            | Int            | The amount of polys that are paid to perform this transaction. |
| mintTxToSign   | Text           | The minting transaction to be signed serialized by the `AssetTransferSerializer` and encoded in JSON. |
| signedMintTx   | Text           | The minting transaction signed serialized by the `AssetTransferSerializer` and encoded in JSON. |
| sendStatus     | SendStatus     | The status of the transaction. It can be: New, Pending, Sent, FailedToSend, and Confirmed. |

##### Choices

##### `SignedAssetMinting_Send`

Marks the transaction as `Pending`.

###### Parameters

None.

##### `SignedAssetMinting_Sent`

Marks a pending transaction with a new status

###### Parameters

| Name          | Type       | Description                                                  |
| ------------- | ---------- | ------------------------------------------------------------ |
| newSendStatus | SendStatus | The new status of this transaction after being sent to the network. |

##### `SignedAssetMinting_Fail`

Marks this transaction as failed

###### Parameters

| Name   | Type | Description                |
| ------ | ---- | -------------------------- |
| reason | Text | The reason of the failure. |

##### `SignedAssetMinting_Confirm`

Marks this transaction as confirmed.

###### Parameters

| Name  | Type | Description                                           |
| ----- | ---- | ----------------------------------------------------- |
| txId  | Text | The identifier of this transaction in the blockchain. |
| depth | Int  | The depth at which the transaction is done.           |

##### `SignedAssetMinting_Archive`

Archives the contract.

###### Parameters

None.

#### `AssetTransferRequest`

The asset transfer request contract models a request from some user to the operator to modify a particular asset (its commit root or its metadata). The asset to modify and the boxNonce where the asset is stored is passed as a parameter at the creation time of the contract.

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested this transfer operation.            |
| someOrgId      | Optional Text  | If this transfer request is related to an organization, this field contains the organization id, otherwise, `None`. |
| from           | [ Text ]       | A list of from addresses encoded in Base58. The addresses from where the assets are going to be minted. |
| to             | [ (Text, Int)] | A list of pairs containing the addresses and the amounts to be transferred to the to addresses. |
| changeAddress  | Text           | The address where the change will be sent after the transaction. |
| assetCode      | AssetCode      | The code of the asset being minted                           |
| quantity       | Int            | The number of assets to mint.                                |
| someCommitRoot | Optional Text  | A commit root encoded in a base 58 string or `None` if there is not commit root. |
| someMetadata   | Optional Text  | Some metadata or `None` if no metadata is needed.            |
| boxNonce       | Int            | The nonce of the box where the asset is stored.              |
| fee            | Int            | The amount of polys that are paid to perform this transaction. |

##### Choices

##### `AssetTransferRequest_Accept`

Accepts the request for further processing by the operator.

###### Parameters

| Name        | Type | Description                                                  |
| ----------- | ---- | ------------------------------------------------------------ |
| txToSign    | Text | The transaction to be signed serialized by the `AssetTransferSerializer` and encoded in JSON. |
| newBoxNonce | Int  | The nonce of the box where the asset is going to be stored.  |

##### `AssetTransferRequest_Reject`

Rejects the request.

###### Parameters

None.

##### `AssetTransferRequest_Archive`

Archives the request. Archive implies that the archiving was not done because of an error.

###### Parameters

None.

#### `UnsignedAssetTransferRequest`

An unsigned asset transfer. It is created when a `AssetTransferRequest_Accept` is exercise in an `AssetTransferRequest`. In includes all the data of the `AssetTransferRequest` and also the transaction to sign  encoded in JSON and the box nonce. 

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested this transfer operation.            |
| someOrgId      | Optional Text  | If this transfer request is related to an organization, this field contains the organization id, otherwise, `None`. |
| from           | [ Text ]       | A list of from addresses encoded in Base58. The addresses from where the assets are going to be minted. |
| to             | [ (Text, Int)] | A list of pairs containing the addresses and the amounts to be transferred to the to addresses. |
| changeAddress  | Text           | The address where the change will be sent after the transaction. |
| assetCode      | AssetCode      | The code of the asset being minted                           |
| quantity       | Int            | The number of assets to mint.                                |
| someCommitRoot | Optional Text  | A commit root encoded in a base 58 string or `None` if there is not commit root. |
| someMetadata   | Optional Text  | Some metadata or `None` if no metadata is needed.            |
| boxNonce       | Int            | The nonce of the box where the asset is going to be stored.  |
| fee            | Int            | The amount of polys that are paid to perform this transaction. |
| txToSign       | Text           | The transfer transaction to be signed, serialized by the `AssetTransferSerializer` and encoded in JSON. |
| msgToSign      | Text           | The message to be signed by the wallet. This has to be sent to the wallet in a separate field.     |

##### Choices

##### `UnsignedAssetTransfer_Sign`

Signs the request for broadcasting to the network by the operator.

###### Parameters

| Name     | Type | Description                                                  |
| -------- | ---- | ------------------------------------------------------------ |
| signedTx | Text | The signed transfer transaction serialized by the `AssetTransferSerializer` and encoded in JSON. |

##### `UnsignedAssetTransfer_Reject`

Rejects the signing request.

###### Parameters

None.

##### `UnsignedAssetTransfer_Archive`

Archives the signing request. Archive implies that the archiving was not done because of an error.

###### Parameters

None.

#### `SignedAssetTransfer`

A signed asset transfer request. It is created when a `UnsignedAssetTransfer_Sign` is exercised in an `UnsignedAssetTransferRequest`. In includes all the data of the `UnsignedAssetTransferRequest` and also the signed transaction encoded in JSON and a send status. The send status informs the current status of the transfer request with respect to the network. 

**Module:** Topl.Asset

##### Parameters

| Name           | Type           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------ |
| operator       | Party          | The operator that will handle this request.                  |
| requestor      | Party          | The party that requested this transfer operation.            |
| someOrgId      | Optional Text  | If this transfer request is related to an organization, this field contains the organization id, otherwise, `None`. |
| from           | [ Text ]       | A list of from addresses encoded in Base58. The addresses from where the assets are going to be minted. |
| to             | [ (Text, Int)] | A list of pairs containing the addresses and the amounts to be transferred to the to addresses. |
| changeAddress  | Text           | The address where the change will be sent after the transaction. |
| assetCode      | AssetCode      | The code of the asset being minted                           |
| quantity       | Int            | The number of assets to mint.                                |
| someCommitRoot | Optional Text  | A commit root encoded in a base 58 string or `None` if there is not commit root. |
| someMetadata   | Optional Text  | Some metadata or `None` if no metadata is needed.            |
| boxNonce       | Int            | The nonce of the box where the asset is going to be stored.  |
| fee            | Int            | The amount of polys that are paid to perform this transaction. |
| txToSign       | Text           | The transfer transaction to be signed serialized by the `AssetTransferSerializer` and encoded in Base58. |
| signedTx       | Text           | The signed transfer transaction serialized by the `AssetTransferSerializer` and encoded in JSON. |
| sendStatus     | SendStatus     | The status of the transaction. It can be: New, Pending, Sent, FailedToSend, and Confirmed. |

##### Choices

##### `SignedAssetTransfer_Send`

Marks the transaction as `Pending`.

###### Parameters

None.

##### `SignedAssetTransfer_Sent`

Marks a pending transaction with a new status

###### Parameters

| Name          | Type       | Description                                                  |
| ------------- | ---------- | ------------------------------------------------------------ |
| newSendStatus | SendStatus | The new status of this transaction after being sent to the network. |

##### `SignedAssetTransfer_Fail`

Marks this transaction as failed

###### Parameters

| Name   | Type | Description                |
| ------ | ---- | -------------------------- |
| reason | Text | The reason of the failure. |

##### `SignedAssetTransfer_Confirm`

Marks this transaction as confirmed.

###### Parameters

| Name  | Type | Description                                           |
| ----- | ---- | ----------------------------------------------------- |
| txId  | Text | The identifier of this transaction in the blockchain. |
| depth | Int  | The depth at which the transaction is done.           |

##### `SignedAssetTransfer_Archive`

Archives the contract.

###### Parameters

None.

