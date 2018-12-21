import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class Process extends UnicastRemoteObject implements ProcessInterface {
    private int ID;
    public boolean Representative;
    private List<Integer> max_received_neighbors = new ArrayList<Integer>(); //Lista de los vecinos que ME ENVIARON el maxID
    private List<Integer> max_send_neighbors = new ArrayList<Integer>(); //Lista de los vecinos que LE ENVIE el maxID
    private boolean text_received = false;
    private int aliveCounter;

    //Variables necesarias para el Algoritmo de eleccion y echos.
    private boolean initiator;
    private boolean commited;
    private boolean echoing;
    private int n;
    private int origen;
    private int confirms;

    public int aliveCounter = 0;

    //Variables importantes para definir vecinos.
    private Integer[] neighborID;
    private ProcessInterface[] neighborRMI;

    public String rutaArchivoCifrado;
    public String ipServidor;
    

    //Inicializador, se crea el servidor RMI de este proceso.
    public Process(int ID, Integer[] neighborID, boolean initiator) throws Exception {
        //Llamada al constructor y los metodos de la clase base (UnicastRemoteObject)
        super();
        this.ID = ID;
        this.neighborID = neighborID;
        this.initiator = initiator;
        this.Representative = false;
        int port = 1200 + ID;
        try {
            LocateRegistry.createRegistry(port);
            Naming.rebind(String.valueOf(ID), this);
        } catch (Exception e) { e.printStackTrace(); }
        System.out.print("Proceso " + ID + " nuevo creado\n");
    }

    //constructor proceso candidato inicial
    //NO TERMINADA
    public Process(int ID, Integer[] neighborID, boolean initiator, String rutaArchivoCifrado, String ipServidor) throws Exception {
        //Llamada al constructor y los metodos de la clase base (UnicastRemoteObject)
        super();
        this.ID = ID;
        this.neighborID = neighborID;
        this.initiator = initiator;
        this.rutaArchivoCifrado = rutaArchivoCifrado;
        this.ipServidor = ipServidor;
        this.Representative = false;
        int port = 1200 + ID;
        try {
            LocateRegistry.createRegistry(port);
            Naming.rebind(String.valueOf(ID), this);
        } catch (Exception e) { e.printStackTrace(); }
        System.out.print("Proceso " + ID + " creado\n");
        Election(this.ID, this.ID);

    }

    public void lookForNeigh() throws Exception {
        this.neighborRMI = new ProcessInterface[neighborID.length];
        for (int i = 0; i < neighborID.length; i++) {
            //Obtenemos los RMI de nuestros vecinos para uso posterior.
            this.neighborRMI[i] = (ProcessInterface) Naming.lookup(String.valueOf(this.neighborID[i]));
        }
    }
    
    public void Initiator() throws Exception{
        System.out.print(this.ID + ": Iniciare una eleccion, le avisare a mis vecinos.\n");
        this.initiator = true;
        this.commited = true;
        this.n = 0;
        lookForNeigh();
        
        for (int i = 0;i < neighborRMI.length ; i++) {
            System.out.print(this.ID + ": Avisando a " + neighborID[i]+"\n");
            neighborRMI[i].Election(this.ID, this.ID);
        }
    }

    @Override
    public void Election(int initID, int callerID) throws Exception{
        new Thread(() -> {
            try{
                if (!commited){
                    lookForNeigh();
                    commited = true;
                    n = 0; 
                    this.origen = callerID;
                    for (int i = 0;i < neighborRMI.length;i++) {
                        if (neighborID[i] == this.origen) {
                            continue;
                        }
                        System.out.print(this.ID + ": ID " + initID + " se candidatea, avisare a " + neighborID[i] + ".\n");
                        neighborRMI[i].Election(initID, this.ID); 
                    }
                }
                n++;
                if (n == neighborRMI.length && !echoing){
                    commited = false;
                    this.Echo(initID);
                }
            } catch(Exception e){}        
        }).start();
    }

    @Override
    public void Echo(int initID) throws Exception{
        new Thread(() -> {
            try{
                if (initID == this.ID) {
                    confirms += 1;
                    if (confirms == neighborID.length) {
                        System.out.print("Soy el representante??\n");
                    }
                } else{
                    for (int i = 0; i < neighborRMI.length ;i++ ) {
                        if (neighborID[i] == origen) {
                            System.out.print(this.ID + ": mandare un Echo a mi origen "+this.origen+".\n");
                            neighborRMI[i].Echo(initID);
                            break;
                        }
                    }
                } 
            } catch(Exception e){}        
        }).start();
    }

   

    //Algoritmo de mensajes de exploracion/eleccion.
    /*public void Election(int callerMaxID, int callerID, int initID) throws Exception {
        lookForNeigh();
        //Tu ID max es mayor a la mia, le avisare a todos mis vecinos menos a ti.
        if (callerMaxID > this.maxID) {
            System.out.print(this.ID + ": Proceso " +callerID + " me ha mandado una nueva MaxID:" + callerMaxID +"\n");
            max_received_neighbors = new ArrayList<Integer>(); //Lista vacia de los vecinos que ME ENVIARON el maxID
            max_send_neighbors = new ArrayList<Integer>(); //Lista vacia de los vecinos que LE ENVIE el maxID
            this.maxID = callerMaxID;

            for (int i = 0; i < neighborID.length; i++) {
                //No llamare a quien me llamo.
                if (neighborID[i] == callerID) {
                    max_received_neighbors.add(callerID); // Guardo quien me envio nuevo maxID
                    if(max_received_neighbors.size() == neighborID.length){
                        //Comienzo Eco
                        System.out.print(this.ID+ ": Todos mis vecinos me enviaron el mismo MaxID\n");
                        System.out.print(this.ID+ ": Comienzo ECO con MaxID:"+ this.maxID +"\n");
                        for (int j = 0; j < neighborID.length; j++) {
                            neighborRMI[j].Echo(this.maxID, this.ID, initID);
                        }
                        break;
                    }
                    continue;
                }
                max_send_neighbors.add(neighborID[i]);
                System.out.print(this.ID + ": Mandando nueva MaxID:" + this.maxID + " a Proceso " + neighborID[i] + "\n");
                neighborRMI[i].Election(this.maxID, this.ID, initID);
                //if(this.maxID != callerMaxID) break;
            }
        }
        //Tu ID maxima es menor a la mia, le avisare a todos los vecinos, MENOS a los que ya les avise
        else if (callerMaxID < this.maxID){
            if(!this.initiator){
                System.out.print(this.ID + ": Proceso " + callerID + " me ha mandado una MaxID:" + callerMaxID +" menor, la mia es: " +this.maxID + "\n"); //por lo que se la mando a todos
            }
            for (int i = 0; i < neighborID.length; i++){
                // Si quien me llamo me envio el maxID o ya se lo envie a este vecino, no le envio mi maxID
                if(max_received_neighbors.contains(neighborID[i]) || max_send_neighbors.contains(neighborID[i])){
                   continue;
                }
                System.out.print(this.ID + ": Mandando mensaje con mi MaxID:" + this.maxID + " a Proceso " + neighborID[i] + "\n");
                neighborRMI[i].Election(this.maxID,this.ID, initID);
                //if(this.maxID != this.ID) break;
            }
        }
        //Si callerMaxID == this.maxID se aumenta en 1 el contador, cuando el contador es igual al nro de vecinos, inicia ECO
        else if (callerMaxID == this.maxID){
            max_received_neighbors.add(callerID); // Guardo quien me envio nuevo maxID
            System.out.print(this.ID + ": Proceso " + callerID +" ha mandado mi misma MaxID:" + callerMaxID +", cuento " + max_received_neighbors.size() + "\n");

            if (max_received_neighbors.size() == neighborID.length){
                //Comienzo Eco
                System.out.print(this.ID+ ":Todos mis vecinos me enviaron el mismo MaxID, comienzo ECO con MaxID:"+ this.maxID +"\n");
                for (int i = 0; i < neighborID.length; i++) {
                    neighborRMI[i].Echo(this.maxID, this.ID, initID);
                }
            }
        }
    }

    //Retorna el MaxID al futuro representante
    public void Echo(int callerMaxID, int callerID, int initID) throws Exception{
        // Si el MaxID recibido es menor al que poseo, anulo el eco
        if(callerMaxID < this.maxID){
            return;
        }

        // Si el MaxID recibido es mayor al que poseo, esto NO deberia pasar
        else if (callerMaxID > this.maxID){
            System.out.print(this.ID + ": Error en el algoritmo \n");
        }

        // Si el MaxID recibido es igual al que poseo, continuo haciendo ECO hasta llegar al representante

        if(callerMaxID == this.maxID) {
            for(int i = 0; i < neighborID.length; i++){
                // Si recibi un ECO de un proceso vecino, NO le envio el ECO
                if(max_send_neighbors.contains(neighborID[i])){
                    continue;
                }
                if(callerMaxID == this.ID){
                    Representative = true;
                    System.out.print(this.ID + ": Soy el Representante, arrodillense!!! \n");
                    return;
                }
                System.out.print(this.ID + ": Proceso " + callerID + " me envio ECO de MaxID:" + this.maxID + "\n");
                System.out.print(this.ID + ": Propago ECO de MaxID:" + this.maxID +" , a Proceso " + neighborID[i] + "\n");
                neighborRMI[i].Echo(this.maxID, this.ID, initID);
            }
        }
    }
    */

    //Crea timer para enviar texto en caso de ser el representante
    public void timerRepresentative() throws Exception {
        TimerTask timerTask = new TimerTask(){
            public void run(){ //Ejecucion
                try {
                    if(Representative){
                        Representative rep = new Representative(ipServidor, rutaArchivoCifrado);
                        rep.key = rep.search_key();
                        System.out.print("La llave publica es: \n" + rep.key + "\n");
                        try {
                            rep.line = rep.read_file();
                            System.out.print("El texto cifrado es: \n" + rep.line + "\n");
                            rep.line = rep.decipher(rep.line, rep.key);
                            System.out.print("El texto descifrado es: \n" + rep.line + "\n");
                            SendText(ID, rep.line);
                            System.out.print("El texto se envio a los demas procesos");
                        } catch(Exception  e) {
                            e.printStackTrace();
                        }
                    }
                    Representative = false;
                }catch(Exception ex) {ex.printStackTrace();}
            }
        };
        Timer timer = new Timer();
        //Inicia la tarea, desde 5seg, cada 5seg
        timer.scheduleAtFixedRate(timerTask, 5000, 5000);
    }

    //Envio de mensaje descifrado a todos los procesos
    public void SendText(int callerID, String text) throws Exception{
        if(ID != callerID){
            // Si recibi un mensaje lo imprimo
            System.out.print(ID + ": El texto decifrado es: " + text + "\n");
        }
        if(!text_received){
            for(int i = 0; i < neighborID.length; i++){
                neighborRMI[i].SendText(ID, text);
            }
        }
        text_received = true;
    }


    //Metodos para ver cuando se muere el representante
    public void NotificarVecinos() throws Exception{
        this.aliveCounter +=1;
        System.out.println(this.ID + ": proceso ha sido notificado por el representante, aumentando el contador a: " + this.aliveCounter);
    }

    public void ReducirCounter() throws Exception{
        this.aliveCounter -=1;
        System.out.println(this.ID + ": ha pasado algo de tiempo, reduciendo mi contador a: " + this.aliveCounter);

        if(aliveCounter < 0){
            Election(this.ID, this.ID);
        }

    }

    //esto debe ser ejectutado por el representante una vez escogido y notificado
    public void createTimerTaskRepresentante(ProcessInterface[] neighborRMI, int[] neighborID, int ID) throws Exception{
        try {
            TimerTask timerTask = new TimerTask(){
                public void run(){ //Ejecucion
                    for(int i = 0; i < neighborRMI.length; i++){
                        System.out.println(ID + ": notificando al proceseo " + neighborID[i] + " de que sigo vivo");
                        try{ neighborRMI[i].NotificarVecinos();}
                        catch(Exception e){e.printStackTrace();}
                    }
                }
            };
            Timer timer = new Timer();
            //Inicia la tarea, desde 0seg, cada 3seg
            timer.scheduleAtFixedRate(timerTask, 0, 5000);
        } catch(Exception ex) {ex.printStackTrace();
        }

    }

    //esto debe ser ejecutado por los vecinos del representante una vez escogido y notificado
    public void createTimerTaskVecino(ProcessInterface interfazPadre){
        try {
            TimerTask timerTask = new TimerTask(){
                public void run(){ //Ejecucion
                    try{interfazPadre.ReducirCounter();}
                    catch (Exception e){e.printStackTrace();}
                }
            };
            Timer timer = new Timer();
            //Inicia la tarea, desde 0seg, cada 3seg
            timer.scheduleAtFixedRate(timerTask, 0, 10000);
        } catch(Exception ex) {ex.printStackTrace();
        }
    }

} //Class end


/*

Los Procesos hacen llamadas de Election a sus vecinos.
Comunicacion via llamadas remotas.

subClase ProcessRMIServer


public void Election(String callerID):
    String[] elected;
    Por neighborIDs not elected;
        Proccess neighbor = GetRMIProccess(idVecinos) //Hace un naminglookup segun el String ID
        Elected.push(neighborIDs)
        neighbor.Election(self.ID)



Llamada Remota
Product c1 = (Product)Naming.lookup("rmi://yourserver.com/toaster")
 */

