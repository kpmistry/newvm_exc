import java.net.*;
import java.io.*;
import java.util.logging.*;

public class EchoServer {
        
		protected int portNumber = 7878;
		protected boolean isStopped = false;
		protected ServerSocket serverSocket = null;
		protected Thread runningThread = null;
		private static final Logger logger = Logger.getLogger(EchoServer.class.getName()); //static as there is only one logger per class, final variables can only be assigned once

		// function go modified from http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
		public void go () {
				synchronized(this){ //obtain lock on the current instance
						this.runningThread = Thread.currentThread(); //get current thread
				}
			openServerSocket();
			while (! isStopped()){
					Socket clientSocket = null;
					try {
							clientSocket = this.serverSocket.accept();
					}catch ( IOException e) {
							if (isStopped()){
									logger.info("Server Stopped");
									return;
							}
							logger.log(Level.SEVERE,"Error accepting Client connection, exception {0}",e);
					}
			
				new Thread(
				new EchoRunner(clientSocket)).start();
				logger.log(Level.INFO,"New Client Connected {0}, {1} Active Threads", new Object [] {clientSocket.getRemoteSocketAddress(), (java.lang.Thread.activeCount()-1)}) ;//new object required to reference multiple parameters in log message, minus 1 to account for main thread
			}
			logger.info("Server Stopped.");
		}
		
   

		public synchronized void stop(){
				this.isStopped = true;
				try {
					this.serverSocket.close();
				} catch (IOException e) {
						logger.log(Level.SEVERE, "Error Closing server, exception: {0}", e);
				}
		}
		
		private void openServerSocket() {
				try {
					this.serverSocket = new ServerSocket(this.portNumber);
				} catch (IOException e) {
					logger.log(Level.SEVERE,"Cannot open port 7878, exception : {0}",e);
				}
		}

		private void closeClientSocket(Socket clientSocket) {
				try {
						clientSocket.close();
						logger.log(Level.INFO,"Client Socket Closed {0}" , clientSocket.getRemoteSocketAddress() );
				}
				catch (IOException e){
				logger.log(Level.SEVERE,"Issue Closing Client Socket {0}",e);
				}
		}

		private synchronized boolean isStopped() {
			return this.isStopped;
		}


		public static void main(String [] args) { //static only belongs to the class
				logger.info("Server Started"); //default logging to console
				Runtime.getRuntime().addShutdownHook(new Thread(){
						public void run() {
								while ((java.lang.Thread.activeCount()) > 3 ){ // as three threads running with no clients
									System.out.println("Waiting to shutdown, " + java.lang.Thread.activeCount() + " active threads");
								}
						}
				}); 
				EchoServer server = new EchoServer();
				server.go();
		}

		// function EchoRunner modified from http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
		public class EchoRunner implements Runnable{
				protected Socket clientSocket = null;
				String inputLine;

				public EchoRunner(Socket clientSocket){
						this.clientSocket = clientSocket;
				}

				public void run() {
						try {
								PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);                   
								BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
								while((inputLine = in.readLine()) != null) {
										logger.log(Level.INFO,"Incoming Characters from {0}",  clientSocket.getRemoteSocketAddress() );
										out.println(inputLine);
										}
								logger.log(Level.INFO,"Client {0} Disconnected",clientSocket.getRemoteSocketAddress()  );
								closeClientSocket(clientSocket);
							}catch (SocketTimeoutException s){
									logger.log(Level.SEVERE, "No response Client Timeout, closing connection {0} ",s);
								closeClientSocket(clientSocket);
							}	catch(IOException e) {
								logger.log(Level.SEVERE,"Exception is {0}",e);
						}
				}

		}


}

