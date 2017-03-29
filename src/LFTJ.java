/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
/**
 *
 * @author s131061
 */
public class LFTJ {

    private int debug = 0; // Represents the amount of debugging. 0 = None, 3 = Extreme
    
    private ArrayList<RelationIterator<Integer>> relIts; //array of iterators, one for each relation
    private ArrayList<ArrayList<Integer>> result; //result set: array with tuples
    private ArrayList<ArrayList<RelationIterator<Integer>>> iteratorPerDepth; // contains which iterator at what dept
    private int p = 0; //iterator pointer
    private int numIters; //number of iterators
    private int depth = -1;
    private int maxDepth = 0; // how deep is our relation? R(x,y), T(y,z) yields 2.
    private int key = 0;
    private boolean atEnd;
    private ArrayList<Integer> currentTuple = new ArrayList<>();
    
    private LFTJ() throws IOException {
        // Create some fictive relations (later to be replaced with existing datasets)
        // Note: it only works if these arrays are sorted
        // Either we have to always sort these arrays first or we need to change the seek method in relation
        Relation rel1 = new Relation(new int[][]{{1,4},{1,5},{1,7},{2,4},{2,8},{3,4},{3,7},{5,5},{6,1},{7,1}});
        Relation rel2 = new Relation(new int[][]{{1,4},{1,5},{1,7},{2,4},{2,8},{3,4},{3,7},{5,5},{6,1},{7,1}});
        Relation rel3 = new Relation(new int[][]{{1,4},{1,5},{1,7},{2,4},{2,8},{3,4},{3,7},{5,5},{6,1},{7,1}});

//        DataImporter di = nmaar ew DataImporter("./data/test.txt");
//        Relation rel1 = di.getRelArray();
//        di = new DataImporter("./data/test.txt");
//        Relation rel2 = di.getRelArray();
//        di = new DataImporter("./data/test.txt");
//        Relation rel3 = di.getRelArray();


        rel1.setUid(1);
        rel2.setUid(2);
        rel3.setUid(3);

        RelationIterator<Integer> rel1iterator = rel1.iterator();
        RelationIterator<Integer> rel2iterator = rel2.iterator();
        RelationIterator<Integer> rel3iterator = rel3.iterator();

        // We define the amount of items in a tuple in the data set..
        // @To-Do: Get this parameter of the set when reading. {1} is 1, {1,5,3} is 3
        maxDepth = 3;

        //create iterators for each relation and put all iterators in an array
        relIts = new ArrayList<>();
        relIts.add(rel1iterator);
        relIts.add(rel2iterator);
        relIts.add(rel3iterator);
        numIters = relIts.size();

        iteratorPerDepth = new ArrayList<>();
        // All values for a, it is only in R1(a,b)
        ArrayList<RelationIterator<Integer>> intermedAListForIterators = new ArrayList<>();
        intermedAListForIterators.add(rel1iterator);
        iteratorPerDepth.add(intermedAListForIterators);
        // All values for b, it is only in R1(a,b) and R2(b,c)
        intermedAListForIterators = new ArrayList<>();
        intermedAListForIterators.add(rel1iterator);
        intermedAListForIterators.add(rel2iterator);
        iteratorPerDepth.add(intermedAListForIterators);
        // All values for c, it is only in R2(b,c) and R3(c,d)
        intermedAListForIterators = new ArrayList<>();
        intermedAListForIterators.add(rel2iterator);
        intermedAListForIterators.add(rel3iterator);
        iteratorPerDepth.add(intermedAListForIterators);
        // All values for d, it is only in R3(c,d)
        intermedAListForIterators = new ArrayList<>();
        intermedAListForIterators.add(rel3iterator);
        iteratorPerDepth.add(intermedAListForIterators);

        //create an array that will hold the results
        result = new ArrayList<>();
    }
    
    
    // Main function of the join. It starts the process
    // @Returns the tuples that were matched
    private void multiJoin(){
        // Start out by initializing the algorithm. This is the main loop.
        leapfrogOpen();

        while (true){ // This is our search function
            if(debug>=2) { printDebugInfo("A"); }

            // If we did not find the specific value we were looking for
            if(atEnd){
                if(debug>=2) { printDebugInfo("B2"); }

                if(depth==0){ // we either stop because we are all the way at the end
                    if(debug>=2) { printDebugInfo("B3"); }
                    break;
                }
                // or we go a level upwards and search for the next value.
                if(debug>=2){System.out.println("Depth "+depth+ " -> Level up, followed by leapfroginit");}
                if(depth!=0){
                    iteratorPerDepth.get(depth).get(findLowestIndex(iteratorPerDepth.get(depth))).next();
                }
                leapfrogUp();
                leapfrogInit();
                if(debug>=2) { printDebugInfo("B4");}

            } else {
                if(debug>=2) { printDebugInfo("C1"); }

                if(depth == maxDepth){ // Als het niet werkt moet je ff -1 erbij zetten
                    boolean testForEnd = false;
                    for(RelationIterator<Integer> relIt : iteratorPerDepth.get(depth)) {
                        if(relIt.atEnd()) {
                            testForEnd = true;
                        }
                    }

                    if(testForEnd){ if(debug>=2) {System.out.println("WE WERE AT THE ENDDDDD");}}

                    // WE GOT A WINNERRR
                    if(debug>=2) { printDebugInfo("C2"); }
                    ArrayList<Integer> tuple = new ArrayList<>();
                    currentTuple.add(key);
                    tuple.addAll(currentTuple);
                    result.add(tuple);
                    currentTuple.remove(currentTuple.size()-1);

                    if(debug>=1) {System.out.println(result); }

                    if(debug>=2){System.out.println("We increase our current iterator by one");}
                    leapfrogNext();
                    if(debug>=2) { printDebugInfo("C3"); }

                } else {

                    if(debug>=2) {System.out.println("C4"); }
                    if(debug>=2){System.out.println("Depth -> Level down");}
                    leapfrogOpen();

                }
            }
        }

        System.out.println(result);
    }

    /**
     * Function which initializes the algorithm.
     * When? At the start after each time a key was found or we went a level higher.
     * Calls? When all iterators are still 'alive' we call leapfrogsearch
     */
    private void leapfrogInit() {
        // Checking if any iterator is empty return (empty) result array
        atEnd = false;
        for(RelationIterator<Integer> relIt : iteratorPerDepth.get(depth)) {
            if(relIt.atEnd()) {
                atEnd = true;
                return;
            }
        }

        // If all iterators are still 'alive' we make sure everything is sorted and start searching for the first
        // possible match.
        if(!atEnd) {
            Collections.sort(iteratorPerDepth.get(depth));
            p = 0;
            leapfrogSearch();
        }
    }

    /**
     * Function which searches for a match in the leapfrogtreejoin. Created as in the paper.
     */
    private void leapfrogSearch() {
        // maxKeyIndex is the index of the maximal element we found and maxKey is the actual value.
        // (Correction needed since -1 % 3 returns -1 and not 2 as we want)
        int maxKeyIndex = ((p - 1) % numIters) + ((p - 1) < 0 ? numIters : 0);
        if(numIters == 1){
            maxKeyIndex = 0;
        }
        int maxKey = iteratorPerDepth.get(depth).get(maxKeyIndex).key();

        if(debug>=2){ System.out.println("Depth: "+depth+" maxKeyIndex: "+maxKeyIndex+" numIters: "+numIters);}
        if(debug>=2) { printDebugInfo("Info van de maxKeyIndex -> " +
                iteratorPerDepth.get(depth).get(maxKeyIndex).debugString());}
        if(debug>=2) { printDebugInfo("We got maxKey = "+maxKey+" and maxKeyIndex = "+maxKeyIndex);}
        
        while (true) {
            // Getting the minimum key, with safe guard to avoid overflow
            if(iteratorPerDepth.get(depth).get(p).atEnd()) {
                atEnd = true;
                return;
            }
            int minKey = iteratorPerDepth.get(depth).get(p).key();
            if(debug>=2) {System.out.println("curIt values: "+iteratorPerDepth.get(depth).get(p).debugString());}

            // If the keys are equal it means we found something where are three are equal. We thus return
            if (maxKey == minKey) {
                key = minKey;
                if(debug>=2) {System.out.println("Found key = "+key);}
                return;
            } 
            // If no common key is found, update pointer of iterator
            else {
                if(debug>=2) {System.out.println("Key not equal, Searching for "+maxKey+" with minkey "+minKey);}

                // We seek for our maxKey, if this is found
//                leapfrogSeek(maxKey);
                iteratorPerDepth.get(depth).get(p).seek(maxKey);
                if(iteratorPerDepth.get(depth).get(p).atEnd()){ // The maxKey is not found
                    if(debug>=2) {System.out.println("key = -1");}
                    atEnd = true;
                    return;
                } else { // The maxKey is found and thus we check if the next iterator can also find it
                    maxKey = iteratorPerDepth.get(depth).get(p).key();
                    p = (p + 1) % numIters;
                    //leapfrogSearch();
                }
            }
        }
    }

    /**
     * Function which sets every iterator at the next value
     * When? - This function is used when we just found a matching tuple which all iterators had.
     * Calls? - Calls leapfrogUp() until at the top.
     * Calls? - Sets the current iterator to the next
     * Calls? - If the current iterator is not at the end, we set the next iterator and execute leapfrogSearch()
     */
    private void leapfrogNext(){
        iteratorPerDepth.get(depth).get(p).next();
        if(iteratorPerDepth.get(depth).get(p).atEnd()){
            atEnd = true;
        } else {
            p = (p + 1) % numIters;
            leapfrogSearch();
        }
    }

    /**
     * Function which seeks for a specific key in the current iterator
     * @param seekKey - The specific value we are searching for.
     * When? - This function is used when we are looking for a specific value??? - NOT USED??!?!?!?!
     * Calls? - Calls leapfrogSearch for the next iterator if we found the key
     */
    private void leapfrogSeek(int seekKey){
        iteratorPerDepth.get(depth).get(p).seek(seekKey);
        if(iteratorPerDepth.get(depth).get(p).atEnd()) {
            //move on to next iterator
            atEnd = true;
        } else {
            p = (p + 1) % numIters;
            leapfrogSearch();
        }
    }

    /**
     * Function which opens up the next level(goes one level down) for all iterators.
     * When? - This function is used when we are going to the next level.
     * Calls? - Calls leapfrogInit afterwards to start up the next search.
     */
    private void leapfrogOpen(){
        if(depth > -1){
            currentTuple.add(iteratorPerDepth.get(depth).get(0).key());
        }
        depth = depth + 1;
        updateIterPandNumIters();
        for(RelationIterator relIt : iteratorPerDepth.get(depth) ) {
            relIt.open();
        }
        leapfrogInit();
    }

    /**
     * Function which opens one level up for every iterator at this specific depth.
     */
    private void leapfrogUp(){
        if(depth > 0){
            currentTuple.remove(currentTuple.size()-1);
        }
        for(RelationIterator relIt : iteratorPerDepth.get(depth) ) {
            relIt.up();
        }
        depth = depth - 1;
        updateIterPandNumIters();
    }

    /**
     * Name says it all right :)!
     */
    private void updateIterPandNumIters(){
        numIters = iteratorPerDepth.get(depth).size();
        if(numIters<p){
            p = 0;
        }
    }

    /**
     * Finds the index in the iteratorPerDepth.get(depth) with the lowest uid of the given iterators.
     */
    private int findLowestIndex(ArrayList<RelationIterator<Integer>> listOfItters){
        int indexOfLowest = 0;
        int lowestValue = listOfItters.get(0).getUid();
        for(int i=0; i<listOfItters.size();i++){
            if(listOfItters.get(i).getUid()<lowestValue){
                indexOfLowest = i;
                lowestValue = listOfItters.get(i).getUid();
            }
        }
        return indexOfLowest;
    }
    /**
     * Finds the index in the iteratorPerDepth.get(depth) with the lowest uid of the given iterators.
     */
    private int findHighestIndex(ArrayList<RelationIterator<Integer>> listOfItters){
        int indexOfHighest = 0;
        int highestValue = listOfItters.get(0).getUid();
        for(int i=0; i<listOfItters.size();i++){
            if(listOfItters.get(i).getUid()>highestValue){
                indexOfHighest = i;
                highestValue = listOfItters.get(i).getUid();
            }
        }
        return indexOfHighest;
    }

    /**
     * Function to print debug information
     */
    private void printDebugInfo(){
        printDebugInfo("");
    }
    private void printDebugInfo(String message){
        if (message.length()>=1){
            System.out.println("Message: "+message);
        }
        if(debug>=3) {
            for (int i = 0; i < iteratorPerDepth.get(depth).size(); i++) {
                System.out.println("Info of iterator " + Integer.toString(i) + ": " +
                        iteratorPerDepth.get(depth).get(i).debugString());
            }
        }
    }

   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Create a LFTJ, load the datasets and ready to rumble
        LFTJ lftj = new LFTJ();
        // We start the jointjes
        lftj.multiJoin();
    }
    
}
