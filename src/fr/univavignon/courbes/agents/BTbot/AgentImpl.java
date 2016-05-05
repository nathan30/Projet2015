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


public class AgentImpl extends Agent
{	
	//ATTRIBUTS

	//l'ennemi le plus proche
	int idE;
	int idIA;
	int pasDuree = 500;//(int) AbstractRoundPanel.PHYS_DELAY;
	int profondeur = 2;
	//affichage debug
	boolean afficherInfosRec = true;
	boolean afficherInfosTps = true;
	boolean afficherInfosDecision = true;
	boolean afficherInfosInitiales = true;
	
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
		
		
		
		idIA = getPlayerId();
		if (idIA == 0) idE = 1;
		else		  idE = 0;
		
		Board board = getBoard();
		
		Direction result = Direction.NONE;
		// partie pas encore commencée : on ne fait rien
		if(board != null)
		{
			if (afficherInfosInitiales)
			{
				System.out.println("idE = " + idE + " idIA = " + idIA);
				System.out.println("duree  du pas = " + pasDuree + " ms");
				System.out.println("distance du pas pour l'IA = " + board.snakes[idIA].movingSpeed * pasDuree + "px");
				
			}
			
			
			if (afficherInfosTps)
			{
				System.out.println("AVANT RECUSRISIVTE");
			}
			
			long tpsDeb = System.currentTimeMillis();
			double[] tab = poids(board, 0, profondeur);
			long tpsFin = System.currentTimeMillis();
			
			if (afficherInfosTps)
			{
				
				System.out.println("APRES RECUSRISIVTE");
				System.out.println("temps d'execution : " + (tpsFin - tpsDeb) + "ms");
			}
			
			int dir = idMax(tab);
			
			if (afficherInfosDecision)
			{
				System.out.println("LEFT\tNONE\tRIGHT	POIDS");
				for (double val : tab)
					System.out.print(val + "\t");
					
				System.out.print("\n");
			}
					
			
			if (dir == 0)
			{
				result =  Direction.LEFT;
			}
			else if (dir == 1)
			{
				result =  Direction.NONE;
			}
			else
			{
				result =  Direction.RIGHT;
			}

		}
		
		if (afficherInfosDecision) System.out.println("DECISION : " + result + "\n");
		
		return result;
	}
	
	//CALCULS DE POIDS

	
	//renvoie un tab qui donne le poids si ia va a gauche, none et droite
	double[] poids(Board bd, int niv, int lim)
	{
		if (afficherInfosRec)
		{
			for (int i = 0; i < niv; i++) System.out.print("\t");
			System.out.println("coo IA : " + bd.snakes[idIA].currentX + "," + bd.snakes[1].currentY + "crntAngle = "+ bd.snakes[idIA].currentAngle + " rad");
		}
		

		
		//CONDITION D'ARRET
		//si l'IA meurt
		if (bd.snakes[idIA].eliminatedBy != null)
		{
			if (afficherInfosRec)
			{
				for (int i = 0; i < niv; i++) System.out.print("\t");
				System.out.println("MORT elimnatedBy : " + bd.snakes[this.getPlayerId()].eliminatedBy);
			}
			
			double[] tab = {-1000,-1000,-1000};
			return tab;
		}
		//si l'ennemi meurt
		if (bd.snakes[idE].eliminatedBy != null)
		{
			if (afficherInfosRec)
			{
				for (int i = 0; i < niv; i++) System.out.print("\t");
				System.out.println("ENNEMI mort elimnatedby = " + bd.snakes[idE].eliminatedBy);
			}
			
			double[] tab = {1000,1000,1000};
			return tab;
		}
		//si on arrive en branche
		else if  (niv == lim)
		{
			if (afficherInfosRec)
			{
				for (int i = 0; i < niv; i++) System.out.print("\t");
				System.out.println("BRANCHE");
			}
			
			double[] tab = {0,0,0};
			return tab;
		}
		//SINON
		else
		{
			//valeur renvoye, le poids pour les 3 mouvement
			double moyenne[] = new double[3];
			int iDir = 0;
			
			//la board qui sera utilise pour le niveau suivant
			//et le tableau qui permettra de recueillir ce qu'elle renvoie
			PhysBoard bdTmp = null;
			double[] pds;
			Direction[] commandes = new Direction[2];
			
			Direction[] mouvements = {Direction.LEFT, Direction.NONE, Direction.RIGHT};
			for (Direction dirIA : mouvements)
			{
				for (Direction dirE : mouvements)
				{
					//ici, on applique a la board passe en param
					//les mouvement geenere
					bdTmp = new PhysBoard((PhysBoard) bd);
					commandes[idIA] = dirIA;
					commandes[idE] = dirE;
					bdTmp.update(pasDuree, commandes);
					
					if (afficherInfosRec)
					{
						for (int i = 0; i < niv; i++) System.out.print("\t");
						System.out.println(" IA = " + dirIA +" et E = " + dirE);
					}
					
					//on calcule le poids de cette nouvelle board
					
					pds = poids(bdTmp, niv+1, lim);
					moyenne[iDir] += moyTab(pds);
				}
				
				moyenne[iDir] = moyenne[iDir] / 3.;
				iDir++;
				
			}
			
			
			return moyenne;
		}
		
	}
	
	//evalue la baord donne
	double[] evaluer(Board bd)
	{
		double[] tab = {500,500,500};
		return tab;
	}
	
	double moyTab(double[] tab)
	{
		double total = 0;
		for (double val : tab)
		{
			total += val;
		}
		
		return (total / tab.length);
	}
	
	//renvoie l'id du nombre max dans le tableau
	
	int idMax(double[] tab)
	{
		//on cherche la valeur max
		int id = 0;
		
		for (int i = 0; i < tab.length; i++)
		{
			//si on trouve une occurence du maximum
			if (tab[id] == tab[i])
			{
				if (Math.random() > 0.5)
					id = i;
			}
			//si on trouve une valeur sup
			if (tab[id] < tab[i])
				id = i;
		}
		
		return id;
	}
}