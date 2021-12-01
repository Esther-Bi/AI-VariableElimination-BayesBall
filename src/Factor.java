import java.util.ArrayList;
import java.util.List;

public class Factor {

    private List<String> parameters = new ArrayList();
    private List<factorRow> rows = new ArrayList();

    Factor(List<String> parameters , List<factorRow> rows){
        this.parameters.addAll(parameters);
        this.rows.addAll(rows);
    }

    Factor(List<String> parameters , List<factorRow> rows , String evidence , String value){ //reduce evidence
        this.parameters.addAll(parameters);
        this.parameters.remove(evidence);
        for (int i=0 ; i<rows.size() ; i++){
            factorRow row = new factorRow(rows.get(i).getP());
            if (rows.get(i).getCases().get(evidence).equals(value)){
                for (String key : rows.get(i).getCases().keySet()) {
                    if (!key.equals(evidence)){
                        row.addParam(key,rows.get(i).getCases().get(key));
                    }
                }
                this.rows.add(row);
            }
        }
    }

    Factor(List<String> given , String[] values , variablesCollection col , int rows , String thisOne){
        this.parameters.addAll(given);
        this.parameters.add(thisOne);
        for (int i=0 ; i<rows ; i++) {
            this.rows.add(new factorRow(Double.parseDouble(values[i])));
        }
        int prev = rows; //8
        for (int i=0 ; i<this.parameters.size() ; i++){
            int num = col.getVariable(this.parameters.get(i)).getOutcome().size();//2
            int howMach = prev/num;//4
            int counter = 0;
            prev = howMach;//4

            int indexOfOutcome = 0;
            for (int j=0 ; j<rows ; j++){
                if (howMach==1 && j!=0){
                    indexOfOutcome = (indexOfOutcome+1) % (col.getVariable(this.parameters.get(i)).getOutcome().size());
                }
                else if (counter == howMach){
                    counter=0;
                    indexOfOutcome = (indexOfOutcome+1) % (col.getVariable(this.parameters.get(i)).getOutcome().size());
                }
                this.rows.get(j).addParam(col.getVariable(this.parameters.get(i)).getName() , col.getVariable(this.parameters.get(i)).getOutcome().get(indexOfOutcome));
                counter++;
            }
        }
    }

    public boolean contains(String vab){
        return this.parameters.contains(vab);
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<factorRow> getRows() {
        return rows;
    }

    public void print() {
        for (int i=0 ; i<this.getRows().size() ; i++){
            this.getRows().get(i).print();
        }
    }
}
