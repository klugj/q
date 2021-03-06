/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.projekt.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Shaun
 */
public class Karteikarte extends DatenbankZugang implements Werte {
      
    private List<Frage> playset =new ArrayList<Frage>(); 
    private List<Frage> wrongset = new ArrayList<Frage>();  // Eine (geordnete) Liste mit ResultSet Objekten, Referenzen auf die Fragen enthalten sind, die der User oft falsch gemacht hat      
    private Player player;
    private String thema;
//gibt die Frage an einer bestimmten Position wieder
    
    public Frage getPlayset(int pos) {
        return playset.get(pos);
    }
    

    public Player getPlayer() {
        return player;
    }
    
    public Karteikarte(){
        
    }
    
    public Karteikarte(String thema, Player player){
        this.thema=thema;
        this.player=player;
    }
    
   
    public int getFrage(){
        try{
            connect();     
            Statement stmt= con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM karten WHERE thema = '"+thema+"';");  // Fragen zu dem gewählten Thema werden gesucht
            wrongset= getDifficult(thema);  // Es wird eine Liste erzeugt, in der die ID's der Karten stehen, die der User oft falsch gemacht hat
            int maxfragen=0; // Maximale Fragen die zu dem Thema verfügbar sind
            int allefragen=0; // Alle Fragen zu jedem Thema, die auf der Datenbank vorhanden sind
            while(rs.next()){
                maxfragen++;  // überprüft wie viele Fragen überhaupt zu dem Thema vorhanden sind
            }
             ResultSet rk = stmt.executeQuery("SELECT * FROM karten;");
             while(rk.next()){
                 allefragen++;
            }
            
            for(int i=0;i<fragenanzahl && i<maxfragen ;i++){
            //________________Einfügen der schweren Fragen_________________________________
                if(i==0 && maxfragen>= fragenanzahl){ // das einfügen der schweren Fragen soll nur einmal geschehen
                  Iterator schwer = wrongset.iterator();
                  
                    for(int j=0;j<schwerefragen && schwer.hasNext();j++){
                            playset.add(wrongset.get(j)); // Fügt die Frage und Antwort dem Set an Fragen hinzu, die dem Nutzer ausgegeben werden
                             i++;                                                                // i ist die gesamtzahl an karten und muss, dadurch das eine schwere Karte hinzukommt, erhöt werden
                    }     
                }
            //_________Es wird anhand der iD überprüft, ob die Karte schon im Set enthalten ist ____________________________________
               
                int  randomid =(int) (allefragen*Math.random());    // randomid gibt eine zuffällige Id einer Karte an. 
              Iterator w = playset.iterator();
         //while(w.hasNext())
                
                while(onSet(randomid) || randomid==0 || onThema(randomid)){
                    randomid =(int) (allefragen*Math.random());
                    
                }
                Statement stmt1= con.createStatement();
                ResultSet rf = stmt.executeQuery("SELECT * FROM karten WHERE thema = '"+thema+"' AND id_karte = '"+randomid+"';");  
            if(rf.next()){
                
                this.playset.add(new Frage(rf.getInt(1),thema,rf.getString(3),rf.getString(4),rf.getString(5),rf.getString(6),rf.getString(7),rf.getString(8),rf.getInt(9)));
               }
            
            }
         Iterator i = playset.iterator();
       
             
        }catch(Exception e){
           System.out.println(e);
           System.out.println("Bin in der Exception");
        }
        close();
        
        return playset.size();    
    }
    //________________________________________________________________________________________-
    
    /* Guckt ob die ID schon in der Liste, der Karten die gespielt werden sollen, enthalten ist*/
    public boolean onSet(int id) throws Exception{
            Statement stmt= con.createStatement();
            Iterator <Frage> j =playset.iterator();
            while(j.hasNext()){
                if(id==j.next().getId()){    // wenn die id einer Frage mit der -id einer Frage im set übereinstimmt, wird true zurückgegeben 
           
                    return true;
                }               
            }
        return false; 
    }
    //__________________________________________________________________________________________________________
    public boolean onThema(int id) throws Exception{
        Statement stmtt= con.createStatement();
        ResultSet rshelp1 = stmtt.executeQuery("SELECT * FROM karten WHERE id_karte = '"+id+"';"); 
        if(rshelp1.next()){
            if(rshelp1.getString(2).equals(thema)){
                return false;
            }     
        }
        return true;
    }
    
        public String getAnswer(int i){
           return playset.get(i).getAnswer1();
        }
       
        public String getQuestion(int i){
             return playset.get(i).getQuestion();
        }
        
        public ResultSet generatCards(int karteid) throws SQLException{
            Statement stmt= con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM karten WHERE id_karte = '"+karteid+"';");
            return rs;
        }
        
       
          public List<Frage> getDifficult(String thema) throws ClassNotFoundException, SQLException {     
            Statement stmt2= con.createStatement();
            Statement stmt1= con.createStatement(); 
            Statement stmt3= con.createStatement(); 
            List<Frage> set =new ArrayList<Frage>();
            ResultSet rf = stmt2.executeQuery("SELECT * FROM relation_beutzer_karten WHERE thema = '"+thema+"' AND id_benutzer = '"+player.getUser_id()+"';");
             //_------------------------------------________________
            
            while(rf.next()){
            
                // Suche die Fragen, welche zu dem thema und welche die Karten-id des rs Resultset haben(Relation zum benutzer)
                 ResultSet rw = stmt3.executeQuery("SELECT * FROM karten WHERE thema = '"+thema+"' AND id_karte = '"+rf.getInt(2)+"';");
                
                 if(rw.next()){
                   
                    set.add(new Frage(rf.getInt(2),thema,rw.getString(3),rw.getString(4),rw.getString(5),rw.getString(6),rw.getString(7),rw.getString(8),rw.getInt(9),rf.getInt(5),rf.getInt(4),rf.getInt(3)));
                    
                 }   
            }
            
            //___________________________________________________________________________
       
            for(int i=0;i<set.size();i++){   // Liste wird sortiert   
                for(int j=i+1;j<set.size();j++){ 
                  Frage help1= (Frage) set.get(i);    
                  Frage help2= (Frage) set.get(j);
                  double low = (double) help1.getMadewrong()/help1.getMaxgespielt();    // In spalte 4 steht die Anzahl, wie oft die Karte falsch gemacht wurde wurde. In spalte 5 wie oft sie gespielt wurde
                  double high =(double) help2.getMadewrong()/help2.getMaxgespielt();
                    if(low<high){   // Wenn die Karte oft falsch gemacht wurde, tausche Sie an die erste stelle der Liste 
                        set.set(i,help2);
                        set.set(j,help1);    
                    }
                   
                }  
            }
          
            return set;     
        }
            
    
 }