package src.blockchain

import java.io.Serializable

class BlockData : Serializable {
    var ID: String? = null
    var Hash: String? = null
    var PrevHash: String? = null
    var Amount = 0f
    var Sender: String? = null
    var Receiver: String? = null
    var Balance = 0f
    var CreatedAt: String? = null
    var PrivateKeyHash: String? = null
}