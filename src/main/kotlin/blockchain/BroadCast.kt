package src.blockchain

import src.models.RequestData
import src.utils.NetUtils

class BroadCast {
    fun Broadcast(requestData: RequestData?) {
        val node: Node = Node()
        val servers: Array<String> = node.GetAllNodes()
        val netUtils = NetUtils()
        //        requestData.CreateWallet.IsBroadcasted = true;
        for (server in servers) {
            netUtils.SendRequest(server, requestData)
        }
    }

    fun BCreateWallet(requestData: RequestData?) {
        // when this broadcasts a createwallet event to other servers
        val node: Node = Node()
        val servers: Array<String> = node.GetAllNodes()
        val netUtils = NetUtils()
        //        requestData.CreateWallet.IsBroadcasted = true;
        for (server in servers) {
            netUtils.SendRequest(server, requestData)
        }
    }
}