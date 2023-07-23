package src.blockchain

import src.models.RequestData
import src.models.ResponseData
import src.utils.CommonUtils
import src.utils.FileUtils
import src.utils.NetUtils
import src.utils.SecUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.List
import kotlin.collections.ArrayList
import kotlin.collections.toTypedArray

class Blockchain {
    var secUtils: SecUtils = SecUtils()
    var commonUtils: CommonUtils = CommonUtils()
    var blockchain: ArrayList<BlockData>? = null
    var fileUtils: FileUtils = FileUtils()

    // this creates a new blocklist for a new wallet
    fun NewChain(address: String, password: String?) {
        blockchain = ArrayList()
        val fileUtils = FileUtils()
        // generate new block
        val blockData = NewGenesisBlock(address, password)
        // add block to chain
        blockchain!!.add(blockData)
        fileUtils.CreateFileObject(blockchain, "data/$address/chain.bin")
    }

    // this creates a new block in the chain
    fun NewGenesisBlock(address: String, password: String?): BlockData {
        val blockData = BlockData()
        blockData.Amount = 100f
        blockData.Balance = 100f
        blockData.ID = secUtils.GeneratePrivateKey(secUtils.GenerateRandString())
        blockData.Sender = "0000000"
        blockData.Receiver = address
        blockData.CreatedAt = commonUtils.CurrentDate()
        blockData.PrevHash = "0000000"
        blockData.PrivateKeyHash = secUtils.GenerateHash(password)
        blockData.Hash = secUtils.GenerateHash(GetBlockHString(blockData, address))
        return blockData
    }

    fun Mint(amount: Float, password: String?, address: String) {
        blockchain = ArrayList()
        val fileUtils = FileUtils()
        // generate new block
        val blockData = BlockData()
        blockData.Amount = amount
        blockData.Balance = amount
        blockData.ID = secUtils.GeneratePrivateKey(secUtils.GenerateRandString())
        blockData.Sender = "0000000"
        blockData.Receiver = address
        blockData.CreatedAt = commonUtils.CurrentDate()
        blockData.PrevHash = "0000000"
        blockData.PrivateKeyHash = secUtils.GenerateHash(password)
        blockData.Hash = secUtils.GenerateHash(GetBlockHString(blockData, address))

        // add block to chain
        blockchain!!.add(blockData)
        fileUtils.CreateFileObject(blockchain, "data/$address/chain.bin")
    }

    fun GetBlockHString(block: BlockData, address: String): String {
        return address + block.Sender + block.Receiver + block.PrevHash
    }

    fun AddBlock(block: BlockData, address: String): Boolean {
        val blocks =
            ArrayList(List.of(*getChain(address)))
        blocks.add(block)
        return fileUtils.CreateFileObject(blocks, "data/$address/chain.bin")
    }

    fun getChain(address: String): Array<BlockData> {
        val obj: Any? = fileUtils.ReadFileObj("data/$address/chain.bin")
        val bkdata = obj as ArrayList<BlockData>
        return bkdata.toTypedArray()
    }

    fun IsBlockExists(address: String, BlockID: String?): Boolean {
        val blocks = getChain(address)
        for (block in blocks) {
            if (block.ID == BlockID) {
                return true
            }
        }
        return false
    }

    fun GetLastBlock(address: String): BlockData {
        val obj: ArrayList<BlockData>? = fileUtils.ReadFileObj("data/$address/chain.bin") as ArrayList<BlockData>?
        var bkdata = ArrayList<BlockData>()
        bkdata = obj as ArrayList<BlockData>
        val blocks = bkdata.toTypedArray()
        //        System.out.println(lastBlock.Hash);
//        System.out.println(lastBlock.Reciever);
        return blocks[blocks.size - 1]
    }

    fun VerifyLastBlock(address: String): Boolean {
        val obj: ArrayList<BlockData>? = fileUtils.ReadFileObj("data/$address/chain.bin") as ArrayList<BlockData>?
        var bkdata = ArrayList<BlockData>()
        bkdata = obj as ArrayList<BlockData>
        val blocks = bkdata.toTypedArray()
        val lastBlock = blocks[blocks.size - 1]
        println(lastBlock.Hash)
        println(lastBlock.Receiver)
        if (blocks.size > 1) {
            val prevBlockData = blocks[blocks.size - 2]
            val hash: String? = secUtils.GenerateHash(GetBlockHString(prevBlockData, address))
            return lastBlock.PrevHash === hash
        }
        if (blocks.size == 1) {
//            System.out.println("One block found ");
            val hash: String? = secUtils.GenerateHash(GetBlockHString(lastBlock, address))
            //            System.out.println("hash "+hash);
//            System.out.println("last block hash "+lastBlock.Hash);
            return lastBlock.Hash == hash
        }
        return false
    }

    fun GetBalance(address: String): Float {
        val blocks = getChain(address)
        var balance = 0f
        val netUtils = NetUtils()

        // prepare request data 
        val requestData = RequestData()
        requestData.action = "GET_BALANCE_SING"
        requestData.balance.walletAddress = address

        // get user balance for current node
        val block = GetLastBlock(address)
        balance = block.Balance
        val consencus = Consencus()
        val datalist = ArrayList<Float>()
        // send request for balance to other nodes 
        val node: Node = Node()
        val allNodes: Array<String> = node.GetAllNodes()
        for (server in allNodes) {
            val resposne: ResponseData = netUtils.SendRequest(server, requestData)
            println("node balance ccc   " + (resposne.data?.get("Balance") ?: 0))
            println("NOde Response  message   " + resposne.respMessage)
            println("NOde Response  code   " + resposne.respCode)
            println("NOde Response  balance   " + resposne.balance)
        }
        //    Consencus.Vote resultVote = consencus.Vote(datalist.toArray());
//    System.out.println("vote count : "+ resultVote.Count);
//    System.out.println("vote data : "+ resultVote.object);
        return balance
    }

    // get balance function for nodes internally 
    // when a node needs a balance on a wallet, it does not need to consult other nodes
    fun GetBalanceSing(address: String): Float {
        val block = GetLastBlock(address)
        return block.Balance
        // get user balance for current node
    }

    // this function transfers money from one wallet to another
    @Throws(Exception::class)
    fun Transfer(
        senderAddress: String, recieverAddress: String, amount: Float,
        isBroadcasted: Boolean, blockID: String?, senderPrivateKey:String?
    ) {
        val wallet = Wallet()
        if (!wallet.IsWalletExists(senderAddress) && !wallet.IsWalletExists(recieverAddress)) {
            throw Exception("Wallet does not exist")
        }
        val error: Error? = null
        // check if sender has the amount of money in question
        if (GetBalance(senderAddress) < amount) {
            throw Exception("Insufficient balance")
        }




        // check if a users vault limit is reached
        if (!CheckWalletLimitOK(senderAddress, amount)) {
            return
        }
        val senderLastBlock = GetLastBlock(senderAddress)
        val recieverLastBlock = GetLastBlock(recieverAddress)

        // check sender password
        if(senderLastBlock.PrivateKeyHash != secUtils.GenerateHash(senderPrivateKey)){

            throw Exception("Invalid private key")
        }
        // check if the transaction has occured on this ledger before

        // new blocks
        val senderBlock = BlockData()
        val recieverBlock = BlockData()
        val newBlockID: String? = secUtils.GenerateHash(secUtils.GenerateRandString())
        senderBlock.Sender = senderAddress
        senderBlock.Receiver = recieverAddress
        senderBlock.Amount = amount
        // senders new balance
        senderBlock.Balance = senderLastBlock.Balance - amount
        senderBlock.CreatedAt = commonUtils.CurrentDate()
        senderBlock.PrevHash = senderLastBlock.Hash
        senderBlock.ID = if (isBroadcasted) blockID else newBlockID
        senderBlock.PrivateKeyHash = senderLastBlock.PrivateKeyHash
        senderBlock.Hash = secUtils.GenerateHash(GetBlockHString(senderBlock, senderAddress))
        recieverBlock.Sender = senderAddress
        recieverBlock.Receiver = recieverAddress
        recieverBlock.Amount = amount
        // reciever new balance
        recieverBlock.Balance = amount + recieverLastBlock.Balance
        recieverBlock.ID = if (isBroadcasted) blockID else newBlockID
        recieverBlock.PrevHash = recieverLastBlock.Hash
        recieverBlock.CreatedAt = commonUtils.CurrentDate()
        recieverBlock.PrivateKeyHash = recieverLastBlock.PrivateKeyHash
        recieverBlock.Hash = secUtils.GenerateHash(GetBlockHString(recieverBlock, recieverAddress))

        // add new blocks to the chain
        val oks = AddBlock(senderBlock, senderAddress)
        val okr = AddBlock(recieverBlock, recieverAddress)
        if (oks && okr) {
            println("broadcasting transfer ...")
            // broadcast new blocks
            val requestData = RequestData()
            val broadCast = BroadCast()
            requestData.action = "TRANSFER"
            requestData.transfer.isBroadcasted = true
            requestData.transfer.amount = amount
            requestData.transfer.receiverAddress = recieverAddress
            requestData.transfer.senderAddress = senderAddress
            requestData.transfer.blockID = senderBlock.ID
            broadCast.Broadcast(requestData)
        } else {
            // send back an error response 
        }
    }

    @Throws(Exception::class)
    fun CheckWalletLimitOK(address: String, amount: Float): Boolean {
        val fileUtils = FileUtils()
        val obj: Any? = fileUtils.ReadFileObj("data/$address/data.bin")
        val walletData: WalletData = obj as WalletData
        val commonUtils = CommonUtils()
        val todaysDate: String = commonUtils.CurrentDate()
        if (!walletData.Vault) {
            return true
        }
        // check if the wallet open day is past
        try {
            if (SimpleDateFormat("yyyy-MM-dd").parse(walletData.VaultOpenDate).before(Date())) {
                // this means its time
                return true
            } else {
                // this means that its not yet time
                // check the limit
                val blocks = getChain(address)
                var sentToday = 0f
                for (block in blocks) {
//                    System.out.println(block.Sender);
                    when (walletData.LimitTime) {
                        "d" -> if (SimpleDateFormat("yyyy-MM-dd").parse(block.CreatedAt)
                            == SimpleDateFormat("yyyy-MM-dd").parse(todaysDate)
                        ) {
//                                System.out.println("todays block ...");
                            if (block.Sender == address) {
                                sentToday = sentToday + block.Amount
                            }
                        }

                        "m" -> //                            System.out.println("monthly");
                            if (SimpleDateFormat("yyyy-MM").parse(block.CreatedAt)
                                == SimpleDateFormat("yyyy-MM").parse(todaysDate)
                            ) {
//                                System.out.println("todays block ...");
                                if (block.Sender == address) {
                                    sentToday = sentToday + block.Amount
                                }
                            }

                        "y" -> if (SimpleDateFormat("yyyy").parse(block.CreatedAt)
                            == SimpleDateFormat("yyyy").parse(todaysDate)
                        ) {
//                                System.out.println("todays block ...");
                            if (block.Sender == address) {
                                sentToday = sentToday + block.Amount
                            }
                        }
                    }
                }

//                System.out.println("spent today  "+ sentToday);
                var ttime = ""
                if (walletData.LimitTime == "m") {
                    ttime = "monthly"
                }
                if (walletData.LimitTime == "d") {
                    ttime = "daily"
                }
                if (walletData.LimitTime == "y") {
                    ttime = "yearly"
                }
                if (sentToday + amount > walletData.VaultLimit) {
//                    System.out.println("limit exceeded");
                    throw Exception("You have exceeeded your limit of " + walletData.VaultLimit + " " + ttime)
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun validateVault(walletData: WalletData, Amount: Float): Boolean {
        return if (!walletData.Vault) {
            false
        } else true
        // check if  the limit has been reached
    }
}