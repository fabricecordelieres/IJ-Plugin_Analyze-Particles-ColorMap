import ij.ImageJ;
import java.io.File;


/**
 * Cette classe permet de lancer ImageJ en utilisant le répertoire où a été créée 
 * la distribution courante du greffon en tant que répertoire "plugins" par défaut.
 * @author fab
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	System.getProperties().setProperty("plugins.dir", System.getProperty("user.dir")+File.separator+"bin"+File.separator);
        ImageJ ij=new ImageJ();
        ij.exitWhenQuitting(true);
    }
}
