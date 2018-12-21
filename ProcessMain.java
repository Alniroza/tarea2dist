import java.io.IOException;
import java.util.*;

public class ProcessMain{

    public static void main(String[] args) throws IOException {
        //Otorga todos los permisos al proceso
        System.setProperty("java.security.policy", "policy");

        int ID = Integer.parseInt(args[0]);
        int maxID = ID;

        String[] tempNeighbor = args[1].split(","); //se separa la lista con los IDs vecinos
        Integer[] neighborID = new Integer[tempNeighbor.length];
        for(int i = 0; i < tempNeighbor.length; i++){
            neighborID[i] = Integer.parseInt(tempNeighbor[i]);
        }

        boolean initiator = Boolean.parseBoolean(args[2]);
        //String rutaArchivoCifrado = args[3];
        String rutaArchivoCifrado = "/home/grupo14/proyecto_linux"; //Quitar de aqui
        //String ipServidor = args[4];
        String ipServidor = "10.10.2.214";
        if(initiator){
            try {
                Process proceso = new Process(ID, neighborID, initiator, rutaArchivoCifrado, ipServidor);
                //Verifica cada cierto tiempo si el proceso es el representante
                //proceso.timerRepresentative();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        } else {
            try {
                Process proceso = new Process(ID, neighborID, initiator);
                //Verifica cada cierto tiempo si el proceso es el representante
                //proceso.timerRepresentative();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
