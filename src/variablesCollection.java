import java.util.HashMap;

public class variablesCollection {

    private HashMap<String, Variable> collection;

    variablesCollection(){
        this.collection = new HashMap<String, Variable>();
    }

    public void addToCollection(String name , Variable vab){
        this.collection.put(name, vab);
    }

    public Variable getVariable(String name){
        return this.collection.get(name);
    }

    public void unColoredAll(){
        this.collection.forEach((k,v) -> v.setColored(false));
    }

    public void unBeenSeenAll(){
        this.collection.forEach((k,v) -> v.setBeenSeenFromChild(false));
        this.collection.forEach((k,v) -> v.setBeenSeenFromFather(false));
    }

    public void setChildes(){
        for (Variable var : this.collection.values()){
            for (int i=0 ; i<var.getGiven().size() ; i++){
                this.collection.get(var.getGiven().get(i)).addChildes(var.getName());
            }
        }
    }

    public HashMap<String, Variable> getCollection() {
        return collection;
    }

    //    @Override
//    public String toString() {
//        String s =
//        return "variablesCollection{" +
//                "collection=" + collection +
//                '}';
//    }
}
