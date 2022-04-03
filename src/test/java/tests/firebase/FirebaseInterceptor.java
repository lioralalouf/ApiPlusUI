package tests.firebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FirebaseInterceptor extends Thread {

    ServerSocket svrsoc;
    Socket soc;
    BufferedReader br;
    String echoin;

    public void run() {
        try {
            svrsoc = new ServerSocket(8086);
            soc = svrsoc.accept();
            br = new BufferedReader (new InputStreamReader(soc.getInputStream()));
            PrintStream ps = new PrintStream(soc.getOutputStream());

            while((echoin=br.readLine())!=null) {
                System.out.println(echoin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
