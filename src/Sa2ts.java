import sa.*;
import ts.*;

public class Sa2ts extends SaDepthFirstVisitor<Void> {

    private Ts tableGlobale;
    private Ts tableLocale;

    private enum TypeVariable {
        PARAMETRE, VARIABLE
    }

    private TypeVariable typeVariable;

    public Sa2ts(SaNode node){
        typeVariable = TypeVariable.VARIABLE;
        tableGlobale = new Ts();
        node.accept(this);

        //Récupéraiton de la fonction main dans la table globale
        TsItemFct main = tableGlobale.getFct("main");
        //Affichage d'un message d'erreur si la fonction n'existe pas
        if (main == null) System.err.println("Fonction main introuvable.");
    }

    //Récupération de la table globale
    public Ts getTableGlobale(){
        tableGlobale.affiche(System.out);
        return tableGlobale;
    }

    //Récupérer la table active
    private Ts getTableActive() {
        return (tableLocale == null) ? tableGlobale : tableLocale;
    }

    //Ajout d'une variable
    @Override
    public Void visit(SaDecVar node){
        //Récupération de la table active
        Ts tableActive = getTableActive();
        //Vérification existance variable ayant même nom dans la table
        if (tableActive.getVar(node.getNom()) == null)
            //Vérification si la variable est un paramètre
            if (typeVariable.equals(TypeVariable.PARAMETRE))
                //Ajout de la variable en tant que paramètre
                node.tsItem = tableActive.addParam(node.getNom());
            else
                //Ajout de la variable dans la table locale
                node.tsItem = tableActive.addVar(node.getNom(),1);
        return null;
    }

    //Appel de variable
    @Override
    public Void visit(SaVarSimple node){
        //Définition d'une variable
        TsItemVar variable;
        //Vérification de la portée de la variable
        if (tableLocale.getVar(node.getNom()) != null) {
            //Récupération de la variable liée
            variable = tableLocale.getVar(node.getNom());
            //Si variable liée introuvable
            if (variable == null)
                //Vérification si paramètre
                if (typeVariable.equals(TypeVariable.PARAMETRE))
                    //Affichage message erreur
                    System.err.println("Paramètre introuvable.");
                //Variable locale
                else
                    //Affichage message erreur
                    System.err.println("Variable introuvable.");
            else
                //Récupération des informations de la variable liée
                node.tsItem = variable;
        } else {
            //Récupération de la variable liée dans la table globale
            variable = tableGlobale.getVar(node.getNom());
            //Variable introuvable ?
            if(variable == null)
                //Affichage message erreur
                System.err.println("Variable introuvable.");
            else
                //Récupération des informations de la variable liée
                node.tsItem = variable;
        }
        return null;
    }

    //Ajout d'une variable de type tableau (toujours variable globale)
    @Override
    public Void visit(SaDecTab node){
        //Vérification s'il n'existe pas une variable ayant le même nom
        // et que la taille du tableau est supérieure à 1
        if (tableGlobale.getVar(node.getNom()) == null && node.getTaille() > 1)
            //Ajout du tableau dans la table globale
            node.tsItem = tableGlobale.addVar(node.getNom(),node.getTaille());
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
        //Vérification que la fonction n'existe pas
        if (tableGlobale.getFct(node.getNom()) == null){
            //Initialisation nouvelle table locale pour la fonction
            tableLocale = new Ts();
            //Récupération des paramètres de la fonction
            SaLDec params = node.getParametres();
            //Nombre de paramètres
            int nombreParams = (params == null) ? 0 : params.length();
            //Si la fonction contient des paramètres
            if (nombreParams > 0) {
                //Variable de type paramètre
                typeVariable = TypeVariable.PARAMETRE;
                //Parcours des paramètres
                params.accept(this);
            }
            //Récupération des variables de la fonction
            SaLDec vars = node.getVariable();
            //S'il y a des variables locales
            if (vars != null) {
                //Variable de type locale
                typeVariable = TypeVariable.VARIABLE;
                //Parcours des variables locales
                vars.accept(this);
            }
            //Parcours des instructions du corps de la fonction
            if (node.getCorps() != null) node.getCorps().accept(this);
            //Ajout de la fonction à la table globale
            node.tsItem = tableGlobale.addFct(node.getNom(),nombreParams,tableLocale,node);
            //Passage de la table locale à null
            tableLocale = null;
        } else {
            //Affichage d'un message d'erreur
            System.err.println("Fonction déjà existante.");
        }
        return null;
    }

    //Appel d'une fonction
    @Override
    public Void visit(SaAppel node){
        //Récupéraiton de la fonction dans la table globale
        TsItemFct fonction = tableGlobale.getFct(node.getNom());
        //Si la fonction est introuvable
        if(fonction == null)
            //Affichage message d'erreur
            System.err.println("Fonction introuvable.");
        else {
            //Récupération du nombre de paramètres
            int nombreParametres = (node.getArguments() == null) ? 0 : node.getArguments().length();
            //Vérification du nombre de paramètres
            if (nombreParametres == fonction.getNbArgs())
                //Récupération des informations de la fonction liée
                node.tsItem = fonction;
            else
                //Affichage message d'erreur
                System.err.println("Nombre d'arguments invalide fonction.");
        }
        return null;
    }
}