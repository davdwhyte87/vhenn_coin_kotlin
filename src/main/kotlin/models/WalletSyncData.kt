package src.models

import src.blockchain.WalletData

//import blockchain.WalletData;
class WalletSyncData {
    var WalletData: WalletData? = null
    var WalletAddress: String? = null
    override fun equals(obj: Any?): Boolean {

        // checking if the two objects
        // pointing to same object
        if (this === obj) return true

        // checking for two condition:
        // 1) object is pointing to null
        // 2) if the objects belong to
        // same class or not
        if (obj == null
            || this.javaClass != obj.javaClass
        ) return false
        val v1 = obj as WalletSyncData // type casting object to the
        // intended class type

        // checking if the two
        // objects share all the same values
        return WalletAddress == v1.WalletAddress
    }
}