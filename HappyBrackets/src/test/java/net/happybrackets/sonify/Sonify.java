package net.happybrackets.sonify;


public class Sonify{

    float inValue;
    float lowInput, lowOutput, highInput, highOutput;
    float offsetIn, offsetOut;
    float rangeIn, rangeOut;
    float outValue;
    double[] data;
    float pastFilterValue, previousDiffValue = 0;
    int dataIndex;



    public Sonify(float lowIn, float highIn, float lowOut, float highOut){

        // Scale values
        lowInput  		= lowIn;
        highInput		= highIn;
        lowOutput		= lowOut;
        highOutput 		= highOut;

        // range In
        offsetIn 		= lowInput;
        rangeIn 		= highInput - lowInput;
        // range Out
        offsetOut 	= lowOutput;
        rangeOut 	= highOutput - lowOutput;
    }

    public void newMethod(){
        System.out.println("new method");
    }

    private void scaleInput(){

        // output calculated
        outValue = (((inValue - offsetIn) / rangeIn)  * rangeOut) + offsetOut;

    }




    public void indexToValue(double inputIndexAsDecimal) {

        if (inputIndexAsDecimal >= 1 |inputIndexAsDecimal < 0){
            System.err.println("inputIndexAsDecimal Error - either 1 or greater or less than 0");
        }
        // getInstance length of data to find index

        dataIndex  = (int) (data.length * inputIndexAsDecimal);
        inValue = (float) data[dataIndex]; // set inValue so update can be called
        scaleInput();
        update();

    }


    public void addData(double[] dataInput ){

        //double[] dataInput = (double[]) dataInputObj;
        double lowIn = dataInput[0];
        double highIn = dataInput[0];

        // calculateValues
        for (int i = 0; i<dataInput.length; i++) {
            lowInput = (float) Math.min(dataInput[i], lowIn);
            highInput = (float) Math.max(dataInput[i], highIn);
        }

        data = dataInput;

}

    public void addValue(float inputVal){

        inValue = inputVal;
        update();

    }

    public void printSonificationAlgorithm() {


        // 5 values spaced across the range
        float[] vals = new float[5];
        for (int i = 0; i < 5; i++){
            System.out.println(offsetIn + " is the offset, and " + rangeIn + " is the range times " + ((float) i) / 5f);
            vals[i] = offsetIn + rangeIn * ((float) i) / 5f;
        }
        System.out.println(vals[3]);
        // State values
        System.out.println("Sonification Algorithm Loaded");
        System.out.println(": Scale values from "  +  lowInput + " to " + highInput );
        System.out.println(": to "  +  lowOutput + " to " + highOutput);
        System.out.println(":");
        System.out.println(":");
        for (int i = 0; i < 5; i ++) {
            this.addValue(vals[i]);
            System.out.println(": for " + vals[i] + " the output value is " + outValue + " and the freq value is " + this.getOutputMTOF());
        }


    }



    private void update(){
        scaleInput();
        // deposit in ring buffer

        // calculate mean of ring buffer

        // calculate median

        // calculate differenced value to 4th order

        // running Max and Min

        // time since last peak

        // time since

    }

    public float getOutput(){

        return outValue;

    }

    public float getOutputMTOF(){

        float mtofOutValue = mtof(outValue);
        return mtofOutValue;

    }



    float mapDifference(float input){
        float output = input - previousDiffValue;
        previousDiffValue = input;
        return output;
    }



    float mapMovingAverage(float input){
        float output = (float) (input * 0.05 + pastFilterValue * 0.95);
        pastFilterValue = output;
        return output;
    }


    float mtof(float input){
        // convert midi note number to a frequency

        float output = (float) (Math.pow(2, (input-69)/12) * 440);
        return output;

    }

    float ftom(float input){
        // convert frequency val to a midi note number

        float output = (float) (69 + (12 *  (Math.log(input/440)/Math.log(2))));
        return output;

    }


}