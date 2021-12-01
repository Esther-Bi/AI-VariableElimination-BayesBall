import java.util.ArrayList;
import java.util.List;

public class factorsCollection {
    private List<Factor> allFactors;

    factorsCollection(){
        this.allFactors = new ArrayList();
    }

    public void addToCollection(Factor f){
        this.allFactors.add(f);
    }

    public void addToCollectionIndex(Factor f , int index){
        this.allFactors.add(index,f);
    }

    public Factor getFactor(int index){
        return this.allFactors.get(index);
    }

    public List<Factor> getAllFactors() {
        return allFactors;
    }

    public void removeFactorContains(String name) {
        for (int i=0 ; i<this.allFactors.size() ; i++){
            if (this.allFactors.get(i).getParameters().contains(name)){
                this.allFactors.remove(i);
                i--;
            }
        }
    }

    public void print() {
        for (int i=0 ; i<this.allFactors.size() ; i++){
            this.allFactors.get(i).print();
            System.out.println("\n");
        }
    }
}
