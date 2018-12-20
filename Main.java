import java.util.*;

public class Main {
    public static void main(String[] args){
        //Otorga todos los permisos al proceso
        System.setProperty("java.security.policy", "policy");
        Integer[] neigh1 = {2,6,5};
        Integer[] neigh2 = {6,1};
        Integer[] neigh3 = {5,7};
        Integer[] neigh4 = {5};
        Integer[] neigh5 = {1,3,4};
        Integer[] neigh6 = {2,1};
        Integer[] neigh7 = {3};

        try {
            System.out.print(java.net.InetAddress.getLocalHost().getHostAddress()+"\n");
            Process process1 = new Process(1,neigh1, false);
            Process process2 = new Process(2,neigh2, false);
            Process process3 = new Process(3,neigh3, false);
            Process process4 = new Process(4,neigh4, false);
            Process process5 = new Process(5,neigh5, false);
            Process process6 = new Process(6,neigh6, false);
            Process process7 = new Process(7,neigh7, false);

            process1.Initiator();
            //System.out.print(process1.maxID+" "+process2.maxID+" "+process3.maxID+" "+process4.maxID+" "+process5.maxID+" "+process6.maxID+" "+process7.maxID);

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
