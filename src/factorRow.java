import java.util.HashMap;
import java.util.List;

public class factorRow {

    private double p;
    private HashMap<String, String> cases;

    factorRow(double p){
        this.p = p;
        this.cases = new HashMap<String, String>();
    }
    public void addParam(String parameter , String outcome){
        this.cases.put(parameter , outcome);
    }

    public double getP() {
        return p;
    }

    public HashMap<String, String> getCases() {
        return cases;
    }

    public boolean sameParam(factorRow r , List<String> parameters){
        boolean flag = true;
        for (int i=0 ; i<parameters.size() && flag ; i++){
            if (!this.getCases().get(parameters.get(i)).equals(r.getCases().get(parameters.get(i)))){
                flag = false;
            }
        }
        if (parameters.size()==0){
            flag = false;
        }
        return flag;
    }

    public void setP(double p) {
        this.p = p;
    }

    public void print() {
        String s = "";
        for (String key : this.getCases().keySet()) {
            s += (key+"="+this.cases.get(key)+" ");
        }
        s = s+""+this.p;
        System.out.println(s);
    }
}
