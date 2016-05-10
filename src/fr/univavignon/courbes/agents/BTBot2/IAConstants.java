package fr.univavignon.courbes.agents.BTBot2;

import java.awt.Color;

public class IAConstants {
	
	//---------------------------------------------------
	//----- ARBRE DE RECHERCHE --------------------------
	//---------------------------------------------------
	
	//DEFENSIF
	public static final int PROFONDEUR = 3;
	public static final long PETIT_PAS_DUREE = 50;//(long) AbstractRoundPanel.PHYS_DELAY;
	public static final int NB_PETIT_PAS = 13;

	
	//---------------------------------------------------
	//--------- POIDS --------- -------------------------
	//---------------------------------------------------

	public static final int MORT_IA = -1000;
	public static final int MORT_ENNEMI = 0;
	public static final int BRANCHE = 0;
	
	//---------------------------------------------------
	//--------- POIDS DES ITEMS -------------------------
	//---------------------------------------------------
	
	////EFFET SUR SOI
	/** Le joueur qui ramasse l'item accélère (bonus) */
	public static final int USER_FAST = 200;
	/** Le joueur qui ramasse l'item ralentit (bonus) */
	public static final int USER_SLOW = 200;
	/** Le joueur qui ramasse l'item vole au dessus des obstacles (bonus) */
	public static final int USER_FLY = 200;
	////	EFFET SUR LES AUTRES JOUEURS
	/** Les autres joueurs accélèrent (malus) */
	public static final int OTHERS_FAST = 200;
	/** Les autres joueurs laissent des trainées plus épaisses (malus) */
	public static final int OTHERS_THICK = 200;
	/** Les autres joueurs ralentissent (malus) */
	public static final int OTHERS_SLOW = 200;
	/** Les commandes des autres joueurs sont inversées (malus) */
	public static final int OTHERS_REVERSE = 200;
	////	EFFET SUR TOUS
	/** La probabilité d'apparition d'un item augmente */
	public static final int COLLECTIVE_WEALTH = 200;
	/** Tous les joueurs peuvent traverser les murs d'enceinte */
	public static final int COLLECTIVE_TRAVERSE = 200;
	/** L'aire de jeu est réinitialisée (les trainées existantes sont effacées) */
	public static final int COLLECTIVE_CLEAN = 200;
}

//PARAMETRES QUI SEMBLENT BIEN
	//DEFENSIF = 
	// lente, pas tres precise mais long terme
	//	petitPasDuree = 30
	// 	nbPetitPas = 13
	//  profondeur = 4
	//  calculMvEnnemi = false
	//OU ENCORE
	// precis tres rapide mais tres court terme
	//	petitPasDuree = 30
	// 	nbPetitPas = 10
	//  profondeur = 3
	//  calculMvEnnemi = false
	//OFFENSIF
	//	??