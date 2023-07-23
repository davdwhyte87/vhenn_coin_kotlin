package src.models

import src.blockchain.Block
import src.blockchain.BlockData

open class ResponseData {
    lateinit var nodeServers: Array<String>
    @JvmField
    var respMessage: String? = null
    @JvmField
    var respCode: Int? = null
    var data: Map<String, Any>? = null
    var balance:Float? = null
    lateinit var nodeWallets: Array<WalletSyncData>

}

class GenericResponseData: ResponseData() {
    var message : String?=null
}

class GetChainResponse: ResponseData(){
    var chain:Array<BlockData> = arrayOf()

}