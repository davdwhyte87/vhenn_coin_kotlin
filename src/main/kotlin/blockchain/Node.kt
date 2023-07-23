package src.blockchain

import com.google.gson.Gson
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import src.models.*
import src.utils.NetUtils
import java.io.*
import java.lang.reflect.TypeVariable
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.List

class Node {
    var dotenv: Dotenv = Dotenv.load()
    var sSocket: Socket? = null

    // this function fetches all the nodes that this server has access to.
    fun GetAllNodes(): Array<String> {
        val jsonParser = JSONParser()
        var serverListObj: JSONObject? = null
        try {
            serverListObj = jsonParser.parse(FileReader("server_list.json")) as JSONObject
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val nodeServers = ArrayList<String>()
        val servers: JSONArray = serverListObj?.get("servers") as JSONArray
        for (server in servers) {
//            System.out.println((String) server);
            nodeServers.add(server as String)
        }
        return nodeServers.toTypedArray()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    fun StartServer() {
        try {
            ServerSocket(dotenv.get("NODE_PORT", "3000").toInt()).use { serverSocket ->

                // int SDK_INT = android.os.Build.VERSION.SDK_INT;
                // if (SDK_INT > 8) {
                //     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                //             .permitAll().build();
                //     StrictMode.setThreadPolicy(policy);
                // }
                // InetAddress IP=InetAddress.getLocalHost();
                // Log.d("SERVER","ip address is :"+ IP.getHostAddress());
                // System.out.println("Server is listening on port " + 4000);
                // Log.d("SERVER","Server is listening on port " + 4000 );
                val IP: InetAddress = InetAddress.getLocalHost()
                println(IP.getHostAddress())
                while (true) {
                    val socket: Socket = serverSocket.accept()
                    println("New client connected")
                    // Log.d("SERVER", "New client connected");
                    val output: OutputStream = socket.getOutputStream()
                    val input: InputStream = socket.getInputStream()
                    val writer = PrintWriter(output, true)
                    val buffer = ByteArray(1024)
                    var read: Int
                    read = input.read(buffer)
                    //


//                ByteArrayInputStream binputStream = new ByteArrayInputStream(buffer);
//                ObjectInputStream objectStream = new ObjectInputStream(input);
//                RequestData requestData = (RequestData) objectStream.readObject();
//                objectStream.close();
//                if (requestData !=null){
//                    System.out.println(requestData.Action);
//                }

//                System.out.println(input.read());
                    if (read != -1) {
//                        GlobalScope.launch {
//                        }
                        val fixedThreadPoolContext = newFixedThreadPoolContext(100, "background")
                        val scope = CoroutineScope(SupervisorJob())

                        scope.launch {
                            withContext(fixedThreadPoolContext) {

                                val outData = String(buffer, 0, read)
                                //                    RequestData requestData = SerializationUtils.deserialize(input);
//                    System.out.println(requestData.Action);
                                // parse data from tcp in text format to json object
                                val jsonParser = JSONParser()
                                val requestObjJson: JSONObject? = null
                                try {
//                        requestObjJson = (JSONObject) jsonParser.parse(outData);
//                        String action = (String) requestObjJson.get("Action");
//                        JSONObject reqData = (JSONObject) requestObjJson.get("Data");
//                        String walletAddress = (String) reqData.get("WalletAddress");
                                    val gson = Gson()
                                    val requestData: RequestData = gson.fromJson(outData, RequestData::class.java)
                                    print("Request data:$requestData")
                                    // perform action based on "actions" in reques
                                    delay(10000)
                                    Route(requestData, writer)
                                    input.close()
                                    //                        System.out.println(action+"{{{{{}}}}}}}");
//                        System.out.println(walletAddress+"wallet address {{{}}}}}}}");
                                } catch (e: Exception) {
                                    val gson = Gson()
                                    val responseData = ResponseData()
                                    responseData.respMessage = "Error occurred"
//                            val respString = gson.toJson(responseData)
                                    val netUtils = NetUtils()
                                    netUtils.SendResponse(writer, responseData)
                                    e.printStackTrace()
                                }
                                //
                                System.out.println(outData);
                            }
//                        runBlocking {
//                            launch{
//
//                            }
//                        }
                        }
                    }
                    //                writer.println("kdkmd");
                }
            }
        } catch (ex: IOException) {
            println("Server exception: " + ex.message)
            // Log.d("SERVER","Server exception: "+ ex.getMessage());
            ex.printStackTrace()
        }
    }

    fun Route(requestData: RequestData, writer: PrintWriter?) {
        when (requestData.action) {
            "CREATE_WALLET" -> {
                println("CREATE WALLET ACTION>>>>>>>>>")
                //                HandleGetServers(writer);

                HandleCreateWallet(writer, requestData)
            }

            "TRANSFER" -> {
                println("TRANSFER ACTION>>>>>>>>>")
                HandleTransfer(writer, requestData)
            }

            "GET_BALANCE" -> {
                println("GET BALANCE ACTION >>>>>>>>>")
                HandleGetBalance(writer, requestData)
            }

            "GET_BALANCE_SING" -> {
                println("GET BALANCE ACTION SING >>>>>>>>>")
                HandleGetBalanceSing(writer, requestData)
            }

            "GET_NETWORK_LIST" -> {}
            "GET_NODE_WALLETS" -> {
                println("GET NODE WALLETS ACTION >>>>>>>>>>>>>")
                HandleGetWallets(writer, requestData)
            }

            "GET_WALLET_CHAIN" -> {
                println("GET WALLET CHAIN >>>>>>>>>>>>>")
                HandleGetChain(writer, requestData)
            }
        }
    }

    fun HandleGetChain(writer: PrintWriter?, requestData: RequestData){
        val address: String? = requestData.walletAddress
        val blockchain = Blockchain()
        val netUtils = NetUtils()
        val responseData = GetChainResponse()
        if (address !=null){
            try {
                val chain = blockchain.getChain(address)

                responseData.respCode = 200
                responseData.chain = chain
                // send reponse
                if (writer != null) {
                    netUtils.SendResponse(writer, responseData)
                }
            }catch(e:Exception){
                val responseData = GetChainResponse()
                responseData.respCode = 500
                responseData.respMessage = e.message.toString()
                // send reponse
                if (writer != null) {
                    netUtils.SendResponse(writer, responseData)
                }
            }
        }else{
            responseData.respCode = 500
            responseData.respMessage = "address not given"
            // send reponse
            if (writer != null) {
                netUtils.SendResponse(writer, responseData)
            }
        }

    }

    fun HandleGetBalance(writer: PrintWriter?, requestData: RequestData) {
        val address: String? = requestData.balance.walletAddress
        val blockchain = Blockchain()
        // get balance for this server
        val balance = blockchain.GetBalance(address!!)
        val netUtils = NetUtils()
        // prepare response
        val responseData = ResponseData()
        responseData.respCode = 200
        responseData.respMessage = "oK"
        responseData.balance = balance
        val dataMap: MutableMap<String, Any> = HashMap()
        dataMap["Balance"] = balance
        responseData.data = dataMap

        // send reponse
        if (writer != null) {
            netUtils.SendResponse(writer, responseData)
        }
    }

    fun HandleGetBalanceSing(writer: PrintWriter?, requestData: RequestData) {
        print("fetching balance... for node ...")
        val address: String? = requestData.balance.walletAddress
        val blockchain = Blockchain()
        // get balance for this server
        val balance = blockchain.GetBalanceSing(address!!)
        print("balance ... for node ...$balance")
        val netUtils = NetUtils()
        // prepare response
        val responseData = ResponseData()
        responseData.respCode = 200
        responseData.respMessage = "oK"
        responseData.balance = balance
        val dataMap: MutableMap<String, Any> = HashMap()
        dataMap["Balance"] = balance
        responseData.data = dataMap

        // send reponse
        if (writer != null) {
            netUtils.SendResponse(writer, responseData)
        }
    }

    fun HandleCreateWallet(writer: PrintWriter?, requestData: RequestData) {
        val wallet = Wallet()
        val broadCast = BroadCast()
        val createWalletReq = CreateWalletReq()
        createWalletReq.WalletName = requestData.createWallet.walletName
        createWalletReq.Password = requestData.createWallet.password
        createWalletReq.WalletVault = requestData.createWallet.isVault
        createWalletReq.LimitTime = requestData.createWallet.limitTime
        createWalletReq.VaultLimit = requestData.createWallet.vaultLimit
        createWalletReq.VaultOpenDate = requestData.createWallet.vaultOpenDate
        println("creating wallet with password " + requestData.createWallet.password)
        val walletAddress: String? = wallet.CreateWallet(requestData.createWallet)
        val netUtils = NetUtils()
        val responseData = ResponseData()
        responseData.respCode = 200
        responseData.respMessage = "oK"
        val data: MutableMap<String, Any> = HashMap()
        if (walletAddress ==null){
            responseData.respCode = 500
            responseData.respMessage = "oK"
        }else {
            data["WalletAddress"] = walletAddress
        }
        println(data)
        responseData.data = data.toMap()
        if (writer != null) {
            netUtils.SendResponse(writer, responseData)
        }

        // send broadcast
        requestData.action = "CREATE_WALLET"
        requestData.createWallet.isBroadcasted = true
        requestData.createWallet.walletAddress = walletAddress
        requestData.createWallet.walletName = createWalletReq.WalletName
        requestData.createWallet.password = createWalletReq.Password
        broadCast.BCreateWallet(requestData)
    }

    fun HandleGetServers(writer: PrintWriter) {
        val servers = GetAllNodes()
        val requestData = RequestData()
        requestData.nodeServers = servers
        requestData.action = "RESP"
        // convert request object to string (json)
        val gson = Gson()
        val reqString: String = gson.toJson(requestData)
        writer.println(reqString)
        println("sending resp")
    }

    fun HandleGetWallets(writer: PrintWriter?, requestData: RequestData?) {
        val wallet = Wallet()
        val netUtils = NetUtils()
        val wallets: Array<WalletSyncData> = wallet.GetWallets()
        val responseData = ResponseData()
        responseData.nodeWallets = wallets
        if (writer != null) {
            netUtils.SendResponse(writer, responseData)
        }
    }

    fun HandleTransfer(writer: PrintWriter?, requestData: RequestData) {
        val blockchain = Blockchain()
        val netUtils = NetUtils()
        if (requestData.transfer.isBroadcasted) {
            println("IS BROADCASTED")
            //            System.out.println(requestData.Transfer.SenderBlockID +"Sender block id");
            if (requestData.transfer.senderAddress?.let { blockchain.IsBlockExists(it, requestData.transfer.blockID) } == true) {
                println("ALREADY EXISTS ")
                val responseData = ResponseData()
                responseData.respCode = 200
                responseData.respMessage = "Ok"
                if (writer != null) {
                    netUtils.SendResponse(writer, responseData)
                }
                return
            }
        }
        try {
            blockchain.Transfer(
                requestData.transfer.senderAddress!!,
                requestData.transfer.receiverAddress!!,
                requestData.transfer.amount, requestData.transfer.isBroadcasted, requestData.transfer.blockID,
                requestData.transfer.senderPrivateKey
            )
            val responseData = ResponseData()
            responseData.respCode = 200
            responseData.respMessage = "Ok"
            if (writer != null) {
                netUtils.SendResponse(writer, responseData)
            }
        } catch (e: Exception) {
            println("Error transfering coins .....  $e")
            val responseData = ResponseData()
            responseData.respCode = 500
            responseData.respMessage = "error sys ....." + e.message
            if (writer != null) {
                netUtils.SendResponse(writer, responseData)
            }
        }
    }

    fun HandleIssueNewCoins(writer: PrintWriter?, requestData: RequestData) {
        val wallet = Wallet()
        wallet.IssueNewCoins(requestData.newCoins.amount, requestData.newCoins.password)
    }

    fun Initialize() {
        // this function starts up a new node

        // get list of all servers from initial peers
        val servers = GetAllNodes()
        for (server in servers) {
            try {
                val addr = server.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                //                System.out.print(addr[0] + "   "+addr[1]);
                val socket = Socket(addr[0], addr[1].toInt())
                val writer = PrintWriter(socket.getOutputStream(), true)
                // convert object to string
                val requestData = RequestData()
                requestData.action = "CREATE_WALLET"
                requestData.walletAddress = "okfkvnjs909sn"
                val gson = Gson()
                val reqString: String = gson.toJson(requestData)
                // write request data
                writer.println(reqString)
                println("sending data to other server")

                //clean up
//                writer.flush();
//                writer.close();
                val input: InputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                var read: Int
                read = input.read(buffer)
                println(read.toString() + "read data size")

//                while (readSize ==-1){
//                    readSize = input.read();
//                    // keeep waiting till we get response
//                }
                if (read != -1) {
                    println("Time to read response .......")
                    val outData = String(buffer, 0, read)
                    println(outData)

                    // get server list from node
//                    String[] servers = GetAllNodes();

                    //get data from req string
//                    Gson gson = new Gson();
                    val responseData: RequestData = gson.fromJson(outData, RequestData::class.java)
                    val jsonObject: JSONObject? = null
                    val jsonParser = JSONParser()
                    var serverListObj: JSONObject? = null
                    try {
                        //read json data on servers
                        serverListObj = jsonParser.parse(FileReader("server_list.json")) as JSONObject
                        val nodeServers = ArrayList<String>()
                        val serverList: JSONArray = serverListObj.get("servers") as JSONArray
                        // add new servers gotten from response
                        serverList.addAll(List.of(*responseData.nodeServers))
                        serverListObj.replace("servers", serverList)
                        // write new updated json object
                        val fileWriter = FileWriter("server_list.json")
                        fileWriter.write(serverListObj.toJSONString())
                        fileWriter.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
//                e.printStackTrace();
                println(e)
            }
            print(server)
        }
    }// loop through wallet and check if there is any it already exists in walletList if not, add it

    // create all the wallets
    // get wallet data from nodes
    val nodeWallets: Unit
        get() {
            val netUtils = NetUtils()
            val servers = GetAllNodes()
            val wallet = Wallet()
            val walletList: ArrayList<WalletSyncData> = ArrayList<WalletSyncData>()
            for (server in servers) {
                val requestData = RequestData()
                requestData.action = "GET_NODE_WALLETS"
                val responseData: ResponseData = netUtils.SendRequest(server, requestData)
                val wallets: Array<WalletSyncData> = responseData.nodeWallets

                // loop through wallet and check if there is any it already exists in walletList if not, add it
                for (syncData in wallets) {
                    if (!walletList.contains(syncData)) {
                        walletList.add(syncData)
                    }
                }
            }

            // create all the wallets
            wallet.CreateMultipleWallets(walletList.toTypedArray())
        }

    companion object {
        private var nodeInstance: Node? = null
        val instance: Node?
            get() {
                if (nodeInstance == null) {
                    nodeInstance = Node()
                }
                return nodeInstance
            }
    }
}