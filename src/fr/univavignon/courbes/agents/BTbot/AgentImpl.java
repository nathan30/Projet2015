package fr.univavignon.courbes.agents.BTbot;

/*
 * Courbes
 * Copyright 2015-16 L3 Info UAPV 2015-16
 * 
 * This file is part of Courbes.
 * 
 * Courbes is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 2 of the License, or (at your option) any later version.
 * 
 * Courbes is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Courbes. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.courbes.agents.Agent;
import fr.univavignon.courbes.common.Board;
import fr.univavignon.courbes.common.Direction;
import fr.univavignon.courbes.common.Position;
import fr.univavignon.courbes.common.Snake;
import fr.univavignon.courbes.inter.simpleimpl.AbstractRoundPanel;
import fr.univavignon.courbes.physics.simpleimpl.PhysBoard;
import fr.univavignon.courbes.physics.simpleimpl.PhysicsEngineImpl;

/**
 * Travail en cours.
 * L'agent fait un BackTracking.
 * 
 * Pour l'instant, lancez le jeu avec que 2 joueurs
 * l'IA est C6PO (star wars)
 * 
 * @author charlie
 *
 */
public class AgentImpl extends Agent
{	
	//ATTRIBUTS

	int idE; //l'id player de l'ennemi
	//(lancez une partie avec 2 joueur pour le moment)
	int idIA;
	
	//pour le backtracking, l'ia fera ses tests en avancant
	//d'un certain pas, caractérirsé par pasDuréé
	//(peut aussi s'exprimer en pixel, en le mul par movingSpeed
	
	//150,4,false semblent pas mal
	int pasDuree = 150;//(int) AbstractRoundPanel.PHYS_DELAY;
	int profondeur = 4; //la profondeur de la recherche
	boolean calculMvEnnemi = false;
	//si true, prend en compte les 3 dir de l'IA et les 3 dir de l'ennemi
	//sinon,prend seulement les 3 dir de l'iA
	
	//affichage debug
	boolean afficherInfosRec = false;
	//affiche l'arbre de recherche, avec les infos
	//ATENTION : l'affichage peut pas mal agrandir le temps de calcul
	boolean afficherInfosInitiales = true;
	//affiche divers infos, et les parametres du backtracking
	//affiche le temps d'execution de la fonction poids() recursive
	//affiche la direction finale prise par l'ia, a la fin de processDirection
		
	//METHODES
	
	/**
	 * Crée un agent contrôlant le joueur spécifié
	 * dans la partie courante.
	 * 
	 * @param playerId
	 * 		Numéro du joueur contrôlé par cet agent.
	 */
	public AgentImpl(Integer playerId) 
	{	super(playerId);
	}
	
	@Override
	public Direction processDirection()
	{	
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		
		
		//on determine les id
		idIA = getPlayerId();
		if (idIA == 0) idE = 1;
		else		  idE = 0;
		
		Board board = getBoard();
		
		Direction result = Direction.NONE;
		// si partie a commence
		if(board != null)
		{
			if (afficherInfosInitiales)
			{
				System.out.println("------------------------------------------------------");
				System.out.println("  idE = " + idE + " idIA = " + idIA);
				System.out.println("  duree  du pas = " + pasDuree + " ms");
				System.out.println("  distance du pas pour l'IA = " + board.snakes[idIA].movingSpeed * pasDuree + "px");
				System.out.println("  soit une vision de  = " + board.snakes[idIA].movingSpeed * pasDuree * profondeur + "px (profondeur de " + profondeur + ")");
				System.out.println("  dist IA pr faire 90° = " + (Math.PI/2.) / board.snakes[idIA].turningSpeed * board.snakes[idIA].movingSpeed + " px");
				System.out.println("----AVANT RECUSRISIVTE-----------------------------");
			}
			
			long tpsDeb = System.currentTimeMillis();
			double[] poids = poids(board, 0, profondeur); //lancement de la fonct recrsiv
			long tpsFin = System.currentTimeMillis();
			
			if (afficherInfosInitiales)
			{
				System.out.println("----APRES RECUSRISIVTE-----------------------------");
				System.out.println("*temps d'execution : " + (tpsFin - tpsDeb) + "ms");
			}
			
			//on determine la direction avec le plus gros poids
			int dir = idMax(poids);
			
			if (afficherInfosInitiales)
			{
				System.out.println("Poids calcules : ");
				System.out.println( " -LEFT  : " + poids[0]);
				System.out.println( " -NONE  : " + poids[1]);
				System.out.println( " -RIGHT : " + poids[2]);
			}
					
			if (dir == 0) 		
				result =  Direction.LEFT;
			else if (dir == 1)	
				result =  Direction.NONE;
			else				
				result =  Direction.RIGHT;

		}
		
		if (afficherInfosInitiales)
		{
			System.out.println(" ***DECISION : " + result + "***");
			System.out.println("------------------------------------------------------\n");
		}
			
		
		return result;
	}
	
	//FONCTIONS CALCULS DE POIDS

	//fonction recursive qui retourne le poids (score)
	//pour chaque directions
	//i = 0 : poids si l'ia va a gauche
	//i = 1 : tout droit
	//i = 2 : a droite
	double[] poids(Board bd, int niv, int lim)
	{
		if (afficherInfosRec)
		{
			for (int i = 0; i < niv; i++) System.out.print("\t");
			System.out.println(niv + "/" + lim + " |  coo IA : (" + bd.snakes[idIA].currentX + "," + bd.snakes[1].currentY + ") crntAngle = "+ bd.snakes[idIA].currentAngle + " rad");
		}
		
		//CONDITION D'ARRET
		
		//si l'IA meurt, on elague
		if (bd.snakes[idIA].eliminatedBy != null)
		{
			if (afficherInfosRec)
			{
				for (int i = 0; i < niv; i++) System.out.print("\t");
				System.out.println("IA MORT elimnatedBy : " + bd.snakes[this.getPlayerId()].eliminatedBy + " poids = -1000");
			}
			
			double[] tab = {-1000,-1000,-1000};
			return tab;
		}
		//si l'ennemi meurt (a prioris, il ne faudrait pas elaguer ici)
		//mais comme on joue 1vs1, si l'ennemi meurt, 
		//l'ia gagne
		else if (bd.snakes[idE].eliminatedBy != null)
		{
			if (afficherInfosRec)
			{
				for (int i = 0; i < niv; i++) System.out.print("\t");
				System.out.println("ENNEMI mort elimnatedby = " + bd.snakes[idE].eliminatedBy + " poids = 1000");
			}
			
			double[] tab = {1000,1000,1000};
			return tab;
		}
		//si on arrive en branche, on evalue la board
		else if  (niv == lim)
		{
			if (afficherInfosRec)
			{
				for (int i = 0; i < niv; i++) System.out.print("\t");
				System.out.println("BRANCHE poids = 0");
			}
			
			return evaluer(bd);
		}
		//SINON
		else
		{
			double moyenne[] = new double[3]; //tab retourne
			int iDir = 0;
			
			//on cree une copie de la board en local
			PhysBoard bdTmp = null;
			
			//enumeration des directions
			double[] pds;
			Direction[] commandes = new Direction[2];
			Direction[] mouvements = {Direction.LEFT, Direction.NONE, Direction.RIGHT};
			for (Direction dirIA : mouvements)
			{
				//si on enumere les dir de l'ennemi
				//on imbrique un autre for
				if (calculMvEnnemi)
				{
					for (Direction dirE : mouvements)
					{
						//on applique a la board les mouvement en cours d'enumeration
						bdTmp = new PhysBoard((PhysBoard) bd);
						commandes[idIA] = dirIA;
						commandes[idE] = dirE;
						bdTmp.update(pasDuree, commandes);
						
						if (afficherInfosRec)
						{
							for (int i = 0; i < niv; i++) System.out.print("\t");
							System.out.println(" -IA = " + dirIA +" et E = " + dirE);
						}
						
						//on calcule le poids de cette nouvelle board
						pds = poids(bdTmp, niv+1, lim);
						moyenne[iDir] += moyTab(pds);
					}
					moyenne[iDir] = moyenne[iDir] / 3.;
				}
				//si on ne prend pas en compte les direction de l'ennemi
				//on le fait simplement avancer tout droit
				else
				{
					
					//on applique a la board les mouvement en cours d'enumeration
					bdTmp = new PhysBoard((PhysBoard) bd);
					commandes[idIA] = dirIA;
					commandes[idE] = null; //null c'est comme Direction.NONE
					bdTmp.update(pasDuree, commandes);
					
					if (afficherInfosRec)
					{
						for (int i = 0; i < niv; i++) System.out.print("\t");
						System.out.println(" -IA = " + dirIA);
					}
					
					//on calcule le poids de cette nouvelle board
					pds = poids(bdTmp, niv+1, lim);
					moyenne[iDir] = moyTab(pds);
				}
				
				if (afficherInfosRec)
				{
					for (int i = 0; i < niv; i++) System.out.print("\t");
					System.out.println(" ->MOYENNE POIDS: " + moyenne[iDir]);
				}
				
				iDir++;
			}
			
			return moyenne;
		}
		
	}
	
	//evalue la board passe en parametre
	
	double[] evaluer(Board bd)
	{
		//pour le moment retourne simplement 0
		double[] tab = {0,0,0};
		return tab;
	}
	
	//fonction qui renvoie la moyenne des valeurs de la table
	double moyTab(double[] tab)
	{
		double total = 0;
		for (double val : tab)
			total += val;
		
		return (total / tab.length);
	}
	
	//renvoie l'id du nombre max dans le tableau
	//si il y a plusieurs occurences du maximum
	//renvoie au hasard l'id de l'une d'elle
	int idMax(double[] tab)
	{
		int id = 0;
		for (int i = 0; i < tab.length; i++)
		{
			//si on trouve une occurence du maximum
			if (tab[id] == tab[i])
				if (Math.random() > 0.5) id = i;

			//si on trouve une valeur sup
			if (tab[id] < tab[i])
				id = i;
		}
		
		return id;
	}
}