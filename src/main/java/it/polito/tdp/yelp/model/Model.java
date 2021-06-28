package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.Adiacenza;
import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	YelpDao dao = new YelpDao();
	Graph<Business,DefaultWeightedEdge> grafo;
	Map<String,Business> idMap = new HashMap<>();
	Business localeMigliore;
	
	// PUNTO 2
	double soglia;
	List<Business> soluzioneMigliore;
	
	public List<String> getAllCity() {
		return this.dao.getAllCity();
	}
	
	public String creaGrafo(String city, Integer anno) {
		this.dao.getAllBusiness(idMap);
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		// Aggiungo i vertici
		this.dao.getVertici(idMap, city, anno);
		Graphs.addAllVertices(this.grafo, this.dao.getVertici(idMap, city, anno));
		
		// Aggiungo gli archi
		for(Adiacenza a : this.dao.getArchi(city, anno, idMap)) {
			Graphs.addEdge(this.grafo, a.getB1(), a.getB2(), a.getPeso());
		}
		
		return "Grafo creato!\n\nNumero vertici: "+this.grafo.vertexSet().size()+"\nNumero archi: "+this.grafo.edgeSet().size();
	}
	
	public Business getLocaleMigliore() {
		double max = 0.0;
		localeMigliore = null;
		
		for(Business v : this.grafo.vertexSet()) {
			double entranti=0.0;
			double uscenti=0.0;
			for(DefaultWeightedEdge in : this.grafo.incomingEdgesOf(v)) 
				entranti += this.grafo.getEdgeWeight(in);
			for(DefaultWeightedEdge out: this.grafo.outgoingEdgesOf(v)) 
				uscenti += this.grafo.getEdgeWeight(out);
			
			if(entranti-uscenti>max) {
				max = entranti-uscenti;
				localeMigliore=v;
			}
			
		}
		
		return localeMigliore;		
	}
	
	
	public List<Business> getVertici(String city,int anno) {
		return this.dao.getVertici(idMap, city, anno);
	}
	
	
	
	// PUNTO 2
	public String cercaPercorso(Business partenza, double soglia) {
		List<Business> parziale = new ArrayList<>();
		parziale.add(partenza);
		this.soluzioneMigliore = null;
		
		this.soglia=soglia;
		
		ricorsione(parziale);
		
		if(this.soluzioneMigliore.size()==0)
			return "\n\nNessun cammino trovato!";
		
		String risultato = "\n\nPercorso trovato partendo da "+partenza+":\n";
		
		for(Business b: this.soluzioneMigliore)
			risultato += b+"\n";
		
		return risultato;
	}

	private void ricorsione(List<Business> parziale) {
		Business ultimoInserito = parziale.get(parziale.size()-1);
		// Caso terminale
		if(ultimoInserito.equals(localeMigliore)) {
			// Se sono alla prima iterazione --> Aggiorno soluzioneMigliore senza controlli
			if(this.soluzioneMigliore==null) {
				this.soluzioneMigliore = new ArrayList<>(parziale);
				return;
			}
			else if(parziale.size()<this.soluzioneMigliore.size()) {
				this.soluzioneMigliore = new ArrayList<>(parziale);
				return;
			}
			else 
				return;
		}
		
		// ... altrimenti
		for(DefaultWeightedEdge arco : this.grafo.outgoingEdgesOf(ultimoInserito)) {
			double peso = this.grafo.getEdgeWeight(arco);
			if(peso>soglia) {
				// Provo a inserirlo
				Business prossimo = Graphs.getOppositeVertex(this.grafo, arco, ultimoInserito);
				
				if(!parziale.contains(prossimo)) {
					List<Business> nuovaParziale = new ArrayList<>(parziale);
					nuovaParziale.add(prossimo);
					ricorsione(nuovaParziale);
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	public String percorsoMigliore(Business partenza, Business arrivo, double soglia) {
		this.soluzioneMigliore = null ;
		
		List<Business> parziale = new ArrayList<Business>() ;
		parziale.add(partenza) ;
		
		cerca(parziale, 1, arrivo, soglia) ;
		
		if(this.soluzioneMigliore.size()==0)
			return "\n\nNessun cammino trovato!";
		
		String risultato = "\n\nPercorso trovato partendo da "+partenza+":\n";
		
		for(Business b: this.soluzioneMigliore)
			risultato += b+"\n";
		
		return risultato;
	}
	
	private void cerca(List<Business> parziale, int livello, Business arrivo, double soglia) {
		
		Business ultimo = parziale.get(parziale.size()-1) ;
		
		// caso terminale: ho trovato l'arrivo
		if(ultimo.equals(arrivo)) {
			if(this.soluzioneMigliore==null) {
				this.soluzioneMigliore = new ArrayList<>(parziale) ;
				return ;
			} else if( parziale.size() < this.soluzioneMigliore.size() ) {
				// NOTA: per calcolare i percorsi più lunghi, basta
				// mettere > nell'istuzione precedente
				this.soluzioneMigliore = new ArrayList<>(parziale) ;
				return ;
			} else {
				return ;
			}
		}
		
		// generazione dei percorsi
		// cerca i successori di 'ultimo'
		for(DefaultWeightedEdge e: this.grafo.outgoingEdgesOf(ultimo)) {
			if(this.grafo.getEdgeWeight(e)>soglia) {
				// vai
				
				Business prossimo = Graphs.getOppositeVertex(this.grafo, e, ultimo) ;
				
				if(!parziale.contains(prossimo)) { // evita i cicli
					parziale.add(prossimo);
					cerca(parziale, livello + 1, arrivo, soglia);
					parziale.remove(parziale.size()-1) ;
				}
			}
		}	
	}
	
	
}
