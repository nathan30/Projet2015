package fr.univavignon.courbes.agents.BTBot2;

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
	Board board = null;
	double [] poids = null;
	Snake snakes[] = null;
	int snakeIAX = 0;
	int snakeIAY = 0;
	double distanceE = Double.POSITIVE_INFINITY; //http://docs.oracle.com/javase/6/docs/api/java/lang/Integer.html
	
	
	int idE; //l'id player de l'ennemi
	//(lancez une partie avec 2 joueur pour le moment)
	int idIA;
	Direction lastDir = Direction.RIGHT;
	
	boolean calculMvEnnemi = false;
	//si true, prend en compte les 3 dir de l'IA et les 3 dir de l'ennemi
	//sinon,prend seulement les 3 dir de l'iA
	
	//affichage debug
	boolean afficherInfosRec = false;
	//affiche l'arbre de recherche, avec les infos
	//ATENTION : l'affichage peut pas mal agrandir le temps de calcul
	boolean afficherInfosInitiales = false;
	//affiche divers infos, et les parametres du backtracking
	//affiche le temps d'execution de la fonction poids() recursive
	//affiche la direction finale prise par l'ia, a la fin de processDirection
	
	
	int agentId = -1;
	
	double []cooSafestArea = new double[2];
		
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
		agentId = playerId;
	}
	
	@Override
	public Direction processDirection()
	{	
		
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		
		//on determine les id
		idIA = getPlayerId();
		
		//if (idIA == 0) idE = 1;
		//else		  idE = 0;
		
		this.board = getBoard();
		
		Direction result = Direction.NONE;
		// si partie a commence
		if(board != null)
		{
			
			if (afficherInfosInitiales)
			{
				double pasDuree = IAConstants.PETIT_PAS_DUREE * IAConstants.NB_PETIT_PAS;
				System.out.println("------------------------------------------------------");
				System.out.println("  idE = " + idE + " idIA = " + idIA);
				System.out.println("  duree PetitPas = " + IAConstants.PETIT_PAS_DUREE + "ms, nb petit pas = " + IAConstants.NB_PETIT_PAS + " -> duree pas = " + pasDuree + " ms");
				System.out.println("  distance PetitPas = " + board.snakes[idIA].movingSpeed * IAConstants.PETIT_PAS_DUREE + "px, , nb petit pas = " + IAConstants.NB_PETIT_PAS + " -> distance pas = " + pasDuree * board.snakes[idIA].movingSpeed + " px");
				
				System.out.println("  soit une vision de  = " + board.snakes[idIA].movingSpeed * pasDuree * IAConstants.PROFONDEUR + "px (profondeur de " + IAConstants.PROFONDEUR + ")");
				System.out.println("  dist IA pr faire 90° = " + (Math.PI/2.) / board.snakes[idIA].turningSpeed * board.snakes[idIA].movingSpeed + " px");
				System.out.println("----AVANT RECUSRISIVTE-----------------------------");
			}
			
			this.snakeIAX =  board.snakes[idIA].currentX;
			this.snakeIAY =  board.snakes[idIA].currentY;
			this.snakes = board.snakes;
			for (Snake snake : snakes){
				
				System.out.println("X ia = " + snakeIAX + ", X e = " + snake.currentX);
				System.out.println("Y ia = " + snakeIAY + ", Y e = " + snake.currentY);
				System.out.println(Math.sqrt((Math.pow((snakeIAX - snake.currentX), 2)) + (Math.pow((snakeIAY - snake.currentY), 2))));
				
				if ( (snake.playerId != idIA) && Math.sqrt((Math.pow((snakeIAX - snake.currentX), 2)) + (Math.pow((snakeIAY - snake.currentY), 2))) <= this.distanceE){
					this.distanceE = (double)Math.sqrt((Math.pow((snakeIAX - snake.currentX), 2)) + (Math.pow((snakeIAY - snake.currentY), 2)));
					idE = snake.playerId;
					
				}
				//System.out.println("DistanceE = " + distanceE);
			}
			
			
			
			/***/
			getSafestArea(board);
			//System.out.println("Safest Area : x => "+cooSafestArea[0]+" y => "+cooSafestArea[1]);
			/***/
			long tpsDeb = System.currentTimeMillis();
			poids = poids(board, 0, IAConstants.PROFONDEUR); //lancement de la fonct recrsive
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
			
			//si la situation est desespre, on garde la precedente
			//parfois, l'ia semble indiquer qu'elle n'a aucune chance de survie
			//pourtant, elle peut s'en sortir
			//astuce pour la faire persister dans le dernier mouvement qu'elle a pris
			//avant de se retoruver dans une situation comme celle ci
			if (poids[0] == -1000 && poids[1] == -1000 && poids[2] == -1000)	
				result = lastDir;
			else if (dir == 0) 		
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
			
		lastDir = result;
		poids = null;
		board = null;
		distanceE = Double.POSITIVE_INFINITY;
		return result;
	}
	
	/**
	 * @param bd
	 * @param niv
	 * @param lim
	 * @return
	 */
	//FONCTIONS CALCULS DE POIDS

	//fonction recursive qui retourne le poids (score)
	//pour chaque directions
	//i = 0 : poids si l'ia va a gauche
	//i = 1 : tout droit
	//i = 2 : a droite
	double[] poids(Board bd, int niv, int lim)
	{
		checkInterruption();
		
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
			
			double[] tab = {IAConstants.MORT_IA,IAConstants.MORT_IA,IAConstants.MORT_IA};
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
			
			double poids = evaluer(bd);
			double[] tab = {poids,poids,poids};
			return tab;
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
						checkInterruption();
						//on applique a la board les mouvement en cours d'enumeration
						bdTmp = new PhysBoard((PhysBoard) bd);
						commandes[idIA] = dirIA;
						commandes[idE] = dirE;
						
						//on applique plusieurs petits pas pour faire le grand pas
						for (int i = 0; i < IAConstants.NB_PETIT_PAS; i++)
							bdTmp.update(IAConstants.PETIT_PAS_DUREE, commandes);
						
						if (afficherInfosRec)
						{
							for (int i = 0; i < niv; i++) System.out.print("\t");
							System.out.println(" -IA = " + dirIA +" et E = " + dirE);
						}
						
						//on calcule le poids de cette nouvelle board
						pds = poids(bdTmp, niv+1, lim);
						moyenne[iDir] += moyTab(pds);
						bdTmp = null;
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
					
					//on applique plusieurs petits pas pour faire le grand pas
					for (int i = 0; i < IAConstants.NB_PETIT_PAS; i++)
						bdTmp.update(IAConstants.PETIT_PAS_DUREE, commandes);
					
					if (afficherInfosRec)
					{
						for (int i = 0; i < niv; i++) System.out.print("\t");
						System.out.println(" -IA = " + dirIA);
					}
					
					//on calcule le poids de cette nouvelle board
					pds = poids(bdTmp, niv+1, lim);
					moyenne[iDir] = moyTab(pds);
					bdTmp = null;
				}
				
				if (afficherInfosRec)
				{
					for (int i = 0; i < niv; i++) System.out.print("\t");
					System.out.println(" ->MOYENNE POIDS: " + moyenne[iDir]);
				}
				
				iDir++;
				bdTmp = null;
			}
			
			return moyenne;
		}
		
	}
	
	//evalue la board passe en parametre
	double evaluer(Board bd)
	{
		
		//valeur de poids renvoye, modifie par les conditions suivantes.
		double poids = 0;
		
		//MORT ENNEMI
		if (bd.snakes[idE].eliminatedBy != null)
		{
			poids += IAConstants.MORT_ENNEMI;
		}
		//ITEMS RECUPEREES
		
		//TO DO

		//ANALYSE SUR LE LONG TERME
		
		//pour le moment retourne simplement 0
		double headX = bd.snakes[agentId].currentX;
		double headY = bd.snakes[agentId].currentY;
		
		double distance = Math.sqrt(Math.pow(headX-cooSafestArea[0], 2) + Math.pow(headY-cooSafestArea[1], 2));
		//System.out.println("distance : "+distance);
//		double[] tab = {1000-distance, 1000-distance, 1000-distance};
//		return tab;
		
		poids += 1000 - distance;
		
		return poids;
	}
	
	
	/**
	* Découper l'aire de jeu en plusieurs parties et calculer des statistiques
	* sur chaque partie découper (Nombre d'item, Variance des corps de snakes...)
	* @return : 
	* 	un tableau contenant les coordonnées du centre de de la meilleure partie de l'aire de jeu 
	*/
	void getSafestArea(Board bd)
	{
		double []coo = new double[2];
		PhysBoard tmpBoard = new PhysBoard((PhysBoard) bd);
		//int [][]totalItem = new int[4][4];
		int []safestArea = new int[3];
		safestArea[0]=0; // Le nombre d'item max
		int upperBoundX, upperBoundY, co = 0;
		// 1- Pour commencer on fait les items 
		if(!tmpBoard.items.isEmpty())
		{
			for(int i=0; i<bd.height; i+=bd.height/4)
			{
				for(int j=0;j<bd.width; j+=bd.width/4)
				{
					upperBoundX = i + bd.height/4-1;
					upperBoundY = j + bd.width/4-1;
					for(int k=0;k<tmpBoard.items.size(); k++)
					{
						if(tmpBoard.items.get(k).x > i && tmpBoard.items.get(k).x<upperBoundX 
													   && tmpBoard.items.get(k).y > j
													   && tmpBoard.items.get(k).y < upperBoundY)
							{
								co++;
							}
					
					}
					if(co > safestArea[0])
					{
						safestArea[0]=co;
						safestArea[1]= bd.height/8 + i;	// Utilisé pour renvoyer le centre de l'aire le plus "sure"
						safestArea[2]= bd.width/8 + j;
					}
					co=0;
							
				}
			}
		}
		cooSafestArea[0] = safestArea[1];
		cooSafestArea[1] = safestArea[2];

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