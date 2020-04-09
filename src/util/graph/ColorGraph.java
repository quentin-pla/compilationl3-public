package util.graph;

import util.intset.*;
import java.util.*;

public class ColorGraph {
    //Graphe à colorer
    public Graph G;
    //Nombre de sommets
    public int R;
    //Nombre de couleurs
    public int K;
    //Sommets
    private Stack<Integer> pile;
    //Sommets enlevés
    public IntSet enleves;
    //Sommets qui débordent
    public IntSet deborde;
    //Tableau des couleurs
    public int[] couleur;
    //Tableau liant chaque sommet avec son identifiant
    public Node[] int2Node;
    //Constante pour désigner les sommets incolores
    static final int NOCOLOR = -1;
    //Nombre de sommets pré-colorés
    private int sommets_pre_colores = 0;

    public ColorGraph(Graph G, int K, int[] phi) {
        this.G = G;
        this.K = K;
        pile = new Stack<Integer>();
        R = G.nodeCount();
        couleur = new int[R];
        enleves = new IntSet(R);
        deborde = new IntSet(R);
        int2Node = G.nodeArray();
        for (int v = 0; v < R; v++) {
            int preColor = phi[v];
            if (preColor >= 0 && preColor < K) {
                couleur[v] = phi[v];
                //Incrémentation des sommets pré-coloriés
                ++sommets_pre_colores;
            }
            else
                couleur[v] = NOCOLOR;
        }
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* associe une couleur à tous les sommets se trouvant dans la pile */
    /*-------------------------------------------------------------------------------------------------------------*/

    public void selection() {
        //Tant que la taille de la pile est supérieure à 0
        while (pile.size() > 0){
            //Récupération de l'identifiant du sommet
            int identifiant_sommet = pile.pop();
            //Récupération des couleurs des voisins du sommet
            IntSet couleurs_voisins = couleursVoisins(identifiant_sommet);
            //Si le nombre de couleurs voisines différentes est différent du nombre de couleurs du graphe
            if (couleurs_voisins.getSize() != K){
                //Coloration du sommet avec une couleur absente de celles contenues dans couleurs_voisins
                couleur[identifiant_sommet] = choisisCouleur(couleurs_voisins);
            }
        }
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* récupère les couleurs des voisins de t */
    /*-------------------------------------------------------------------------------------------------------------*/
    public IntSet couleursVoisins(int t) {
        //Récupération du sommet à partir de son identifiant
        Node sommet = int2Node[t];
        //Instanciation d'une liste contenant les couleurs voisines
        IntSet result = new IntSet(R);
        //Récupération des sommets voisins du sommet
        NodeList sommets_voisins = sommet.succ();
        //Tant qu'il reste des voisins
        while(sommets_voisins != null){
            //Récupération de la tête de liste
            Node sommet_voisin = sommets_voisins.head;
            //Couleur du sommet
            int couleur_sommet = couleur[sommet_voisin.mykey];
            //Si le sommet est coloré
            if (couleur_sommet != NOCOLOR)
                //On ajoute la couleur à la liste
                result.add(couleur_sommet);
            //Passage au voisin suivant
            sommets_voisins = sommets_voisins.tail;
        }
        //Retour du résultat
        return result;
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* recherche une couleur absente de colorSet */
    /*-------------------------------------------------------------------------------------------------------------*/

    public int choisisCouleur(IntSet colorSet) {
        //Pour chaque couleur disponible dans le graphe
        for (int couleur = 0; couleur < K ; couleur++)
            //Si la couleur n'est pas contenue dans l'ensemble
            if(!colorSet.isMember(couleur))
                //Retour de la couleur
                return couleur;
        //Pas de couleur disponible, on retourne la constante
        return NOCOLOR;
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* calcule le nombre de voisins du sommet t */
    /*-------------------------------------------------------------------------------------------------------------*/

    public int nbVoisins(int t) {
        //Récupération du sommet grâce à son identifiant
        Node sommet = int2Node[t];
        //Nombre de voisins
        int nombre_voisins = 0;
        //Récupération des sommets voisins du sommet
        NodeList sommets_voisins = sommet.succ();
        //Tant qu'il reste des voisins
        while(sommets_voisins != null){
            //Récupération de la tête de liste
            Node sommet_voisin = sommets_voisins.head;
            //Si le sommet voisin n'est pas un sommet enlevé ou qui déborde
            if(!enleves.isMember(sommet_voisin.mykey))
                //Incrémentation du nombre de voisins
                ++nombre_voisins;
            //Passage au voisin suivant
            sommets_voisins = sommets_voisins.tail;
        }
        //Retour du nombre de voisins final
        return nombre_voisins;
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /* simplifie le graphe d'interférence g                                                                        */
    /* la simplification consiste à enlever du graphe les temporaires qui ont moins de k voisins                   */
    /* et à les mettre dans une pile                                                                               */
    /* à la fin du processus, le graphe peut ne pas être vide, il s'agit des temporaires qui ont au moins k voisin */
    /*-------------------------------------------------------------------------------------------------------------*/

    public void simplification() {
        //Récupération du nombre de sommets sans pré-couleur
        int sommets_incolores = R - sommets_pre_colores;
        //Booléen modification
        boolean modification = true;
        //Tant que la taille de la pile est inférieur au nombre de sommets incolores
        while(pile.size() != sommets_incolores){
            //Tant que c'est en modification
            if (modification) {
                //Passage du booléen à faux
                modification = false;
                //Récupération des sommets présents dans le graphe à colorier
                Node[] sommets = G.nodeArray();
                //Pour chaque sommet dans la liste
                for (Node sommet : sommets) {
                    //Récupération de l'identifiant du sommet
                    int identifiant_sommet = sommet.mykey;
                    //Si le nombre de voisins du sommet est inférieur au nombre de couleurs
                    // et que le sommet est incolore et que le sommet n'est pas enlevé
                    if ((nbVoisins(identifiant_sommet) < K)
                        && (couleur[identifiant_sommet] == NOCOLOR)
                        && (!enleves.isMember(identifiant_sommet))) {
                        //AJout du sommet dans la liste
                        pile.push(identifiant_sommet);
                        //Enlèvement du sommet
                        enleves.add(identifiant_sommet);
                        //Passage du booléen à vrai
                        modification = true;
                    }
                }
            }
            else break;
        }
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------------*/

    public void debordement() {
        //Tant que la taille de la pile est plus petite que le nombre de sommets total
        while(pile.size() != R - sommets_pre_colores){
            //Pour chaque sommet dans le graphe
            for (Node sommet : G.nodeArray()) {
                //Récupération de l'identifiant du sommet
                int identifiant_sommet = sommet.mykey;
                //Si le sommet n'est pas enlevé et qu'il ne déborde pas
                // et qu'il est incolore
                if (!enleves.isMember(identifiant_sommet)
                    && !deborde.isMember(identifiant_sommet)
                    && couleur[identifiant_sommet] == NOCOLOR){
                    //Ajout du sommet à la liste
                    pile.push(identifiant_sommet);
                    //Ajout du sommet à la liste de ceux enlevés
                    enleves.add(identifiant_sommet);
                    //Ajout du sommet à la liste de ceux qui débordent
                    deborde.add(identifiant_sommet);
                    //Simplification
                    simplification();
                    break;
                }
            }
        }
    }

    /*-------------------------------------------------------------------------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------------*/

    public void coloration() {
        this.simplification();
        this.debordement();
        this.selection();
    }

    public void affiche() {
        System.out.println("vertex\tcolor");
        for (int i = 0; i < R; i++) {
            System.out.println(i + "\t" + couleur[i]);
        }
    }
}
