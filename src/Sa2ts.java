import sa.*;
import ts.*;

public class Sa2ts extends SaDepthFirstVisitor<Void> {

    private Ts tableGlobale;
    private Ts tableLocale;

    public Sa2ts(SaNode node){
        tableGlobale = new Ts();
        tableLocale = tableGlobale;
        node.accept(this);
    }

    //Récupération de la table globale
    public Ts getTableGlobale(){
        tableGlobale.affiche(System.out);
        return tableGlobale;
    }

    //Ajout d'une variable
    @Override
    public Void visit(SaDecVar node){
        //Vérification existance variable ayant même nom
        if (tableLocale.getVar(node.getNom()) == null)
            //Vérification si la variable est un paramètre
            if (node.tsItem != null && node.tsItem.isParam)
                //Ajout de la variable en tant que paramètre
                tableLocale.addParam(node.getNom());
            else
                //Ajout de la variable dans la table locale
                tableLocale.addVar(node.getNom(),1);
        return null;
    }

    //Appel de variable
    @Override
    public Void visit(SaVarSimple node){
        //Vérification de la portée de la variable
        if (node.tsItem.portee != tableGlobale) {
            //Récupération de la table contenant la variable
            Ts table = tableGlobale.getTableLocale(node.tsItem.portee.toString());
            //Récupération de la variable liée
            TsItemVar var = table.getVar(node.getNom());
            //Si variable liée introuvable
            if (var == null)
                //Vérification si paramètre
                if (node.tsItem.isParam)
                    //Affichage message erreur
                    System.err.println("Paramètre introuvable.");
                else
                    //Affichage message erreur
                    System.err.println("Variable introuvable.");
            else
                //Récupération des informations de la variable liée
                node.tsItem = var;
        } else {
            //Récupération de la variable liée dans la table globale
            TsItemVar var = tableGlobale.getVar(node.getNom());
            //Variable introuvable ?
            if(var == null)
                //Affichage message erreur
                System.err.println("Variable introuvable.");
            else
                //Récupération des informations de la variable liée
                node.tsItem = var;
        }
        //Ajout de la varible dans la table globale
        this.tableGlobale.addVar(node.getNom(),node.tsItem.getTaille());
        return null;
    }

    //Ajout d'une variable de type tableau (toujours variable globale)
    @Override
    public Void visit(SaDecTab node){
        //Vérification s'il n'existe pas une variable ayant le même nom
        if(tableGlobale.getVar(node.getNom()) == null)
            //Ajout du tableau dans la table globale
            tableGlobale.addVar(node.getNom(),node.getTaille());
        return null;
    }

    //Récupération d'une variable à partir d'un indice dans un tableau
    @Override
    public Void visit(SaVarIndicee node){
        //Récupération variable dans la table globale
        TsItemVar var = tableGlobale.getVar(node.getNom());
        //Si variable introuvable
        if(var == null)
            //Affichage message d'erreur
            System.err.println("Tableau introuvable.");
        else
            //Récupération des informations de la variable liée
            node.tsItem = var;
        return null;
    }

    //Ajout d'une fonction
    @Override
    public Void visit(SaDecFonc node){
        //Vérification fonction même nom n'existe pas
        if (tableLocale.getFct(node.getNom()) == null){
            //Initialisation nouvelle table locale pour la fonction
            tableLocale = new Ts();
            //Récupération des paramètres de la fonction
            SaLDec params = node.getParametres();
            //Nombre de paramètres
            int nombreParams = (params == null)?0:params.length();
            //Si la fonction contient des paramètres
            if (nombreParams > 0)
                //Pour chaque paramètre
                while(params != null){
                    //Récupération du paramètre
                    SaDec parametre = params.getTete();
                    //Ajout d'un paramètre dans la table locale de la fonction
                    tableLocale.addParam(parametre.getNom());
                    //Passage au paramète suivant
                    params = params.getQueue();
                }
            //Récupération des variables de la fonction
            SaLDec vars = node.getVariable();
            //S'il y a des variables
            if (vars != null)
                //Ajout des variables à la table locale
                vars.accept(this);
            //Ajout de la fonction à la table globale
            this.tableGlobale.addFct(node.getNom(),nombreParams,tableLocale,node);
        } else {
            //Affichage d'un message d'erreur
            System.err.println("Fonction déjà existante.");
        }
        return null;
    }

    //Appel d'une fonction
    @Override
    public Void visit(SaAppel node){
        //Récupéraiton du main dans la table globale
        TsItemFct main = tableGlobale.getFct("main");
        //Récupéraiton de la fonction dans la table globale
        TsItemFct fonction = tableGlobale.getFct(node.getNom());
        //Si la fonction est introuvable
        if(main == null || fonction == null)
            //Affichage message d'erreur
            System.err.println("Fonction introuvable.");
        else
            //Vérification du nombre d'arguments
            if (node.getArguments().length() == fonction.getNbArgs())
                //Récupération des informations de la fonction liée
                node.tsItem = fonction;
            else
                //Affichage message d'erreur
                System.err.println("Nombre d'arguments invalides fonction.");
        return null;
    }
}