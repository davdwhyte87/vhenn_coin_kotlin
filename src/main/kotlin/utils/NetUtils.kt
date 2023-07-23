package src.utils

import com.google.gson.Gson
import src.models.RequestData
import src.models.ResponseData
import java.io.PrintWriter
import java.net.Socket

class NetUtils {
    // this takes a request object and sends it over a tcp network and get response
    fun SendRequest(Ip: String, requestData: RequestData?): ResponseData {
        var responseData = ResponseData()
        val errResponse = ResponseData()
        val gson = Gson()
        try {
            val addr = Ip.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            //                System.out.print(addr[0] + "   "+addr[1]);
            val socket = Socket(addr[0], addr[1].toInt())
            val writer = PrintWriter(socket.getOutputStream(), true)

            // convert req data object to string
            val reqString = gson.toJson(requestData)
            // write request data
            writer.println(reqString)
            println("sending data to other server ( $Ip )")
            val input = socket.getInputStream()
            val buffer = ByteArray(1024)
            val read: Int
            read = input.read(buffer)
            println(read.toString() + "read data size")
            if (read != -1) {
                println("Time to read response .......")
                val outData = String(buffer, 0, read)
                println(outData)
                //get data from req string
                responseData = gson.fromJson(outData, ResponseData::class.java)
            }


            // close all sockets 
            writer.close()
            input.close()
            socket.close()
        } catch (e: Exception) {
//                e.printStackTrace();
            errResponse.respCode = 500
            errResponse.respMessage = e.message
            println(e)

            return errResponse
        }
        return responseData
    }

    // this function takes a request socket steam and sends a response through the output stream
    fun SendResponse(writer: PrintWriter, responseData: ResponseData?) {
        val gson = Gson()
        val reqString = gson.toJson(responseData)
        writer.println(reqString)
        writer.close()
    }
}