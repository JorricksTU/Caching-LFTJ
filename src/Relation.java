/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author s131061
 */
public class Relation implements Iterable<Integer>{
    private int[][] array;
    private int arrayDepth;
    private int uid; // UID

    public void setUid(int uid) {
        this.uid = uid;
    }
    
    Relation(int[][] relArray) {
        this.array = relArray;
//        printArray(this.array);
        this.arrayDepth = array.length;
    }

    private static void printArray(int[][] anArray) {
        System.out.println("---Printing array of iterator---");
        for (int i = 0; i < anArray.length; i++) {
            System.out.print("[");
            boolean first = true;
            for (int j = 0; j < anArray[0].length; j++){
                if(first){
                    first = false;
                } else {
                    System.out.print(", ");
                }
                System.out.print(anArray[i][j]);
            }
            System.out.print("]");
        }
        System.out.println("\n---Stop printing array---");
    }


    @Override
    public RelationIterator<Integer> iterator() {
       RelationIterator<Integer> relIt = new RelationIterator<Integer>() {

           //not sure that both index and key are needed, but it seems to work fine
           private int indexDepth = -1; // From depth 0 to ?(often 1 as we would have just 2 data points)
           private int indexTuple = 0; // From tuple 0 to n
           // specifies for a certain depth where the tree's max is
           private int tupleLowerBound = 0;
           private int tupleUpperBound = (arrayDepth == 0? 0 : array.length-1);
           private int key;

           @Override
           public int key() {
               key = array[indexTuple][indexDepth];
               return key;
           }

           @Override
           public boolean hasNext() {
               return indexTuple < tupleUpperBound;
           }

           @Override
           public Integer next() {
               indexTuple ++;
               return key;
           }

           //position iterator at least upper bound for seekKey as explained in the paper
           //or at the end if no such key exists
           @Override
           public void seek(int seekKey) {
               while (key < seekKey && indexTuple <= tupleUpperBound+1) {
                   if(!atEnd()){
                       indexTuple ++;
                       if(indexTuple <= tupleUpperBound){
                           key = array[indexTuple][indexDepth];
                       }
                   } else {
                       return;
                   }
               }
           }

           @Override
           public boolean atEnd() {
               return (indexTuple > tupleUpperBound);
           }

           //used for sorting
           @Override
           public int compareTo(RelationIterator<Integer> o) {
               return Integer.compare(this.key(), o.key());
           }

           //proceeds to the first element at the next depth
           @Override
           public void open(){
               if(indexDepth < array[0].length - 1){
                   indexDepth++;
                   findTupleLowerBound();
                   findTupleUpperBound();
               } else {
                   throw new NoSuchElementException("indexDepth =" + indexDepth);
               }
           }

           //returns to the parent key at the previous depth
           @Override
           public void up(){
               if(indexDepth >= 0){
                   indexDepth--;
                   if(indexDepth < 0){
                       indexTuple = 0;
                   }
                   findTupleLowerBound();
                   findTupleUpperBound();
               } else {
                   throw new NoSuchElementException();
               }
           }

           //returns the lowest tuple index of all elements having the same parents(in the tree)
           void findTupleLowerBound(){
               if(indexDepth > 0) {
                   tupleLowerBound = 0;
                   int searchDepth = indexDepth - 1;
                   for (int i = searchDepth; i >= 0; i--) {
                       if(array.length <= indexTuple){
                           tupleLowerBound = indexTuple;
                       }
                       else {
                           int levelKey = array[indexTuple][searchDepth];
                           for (int j = indexTuple; j >= 0 && array[j][searchDepth] > tupleLowerBound; j--) {
                               if (levelKey != array[j][searchDepth]) {
                                   tupleLowerBound = j - 1;
                                   break;
                               }
                           }
                       }
                   }
               } else{
                   tupleLowerBound = 0;
               }
           }

           //returns the lowest tuple index of all elements having the same parents(in the tree)
           void findTupleUpperBound(){
               if(indexDepth > 0){
                   tupleUpperBound = array.length-1;
                   int searchDepth = indexDepth - 1;
                   for(int i = searchDepth; i >= 0; i--){
                       if(array.length <= indexTuple){
                           tupleUpperBound = array.length-1;
                       }
                       else {
                           int levelKey = array[indexTuple][searchDepth];
                           for(int j = indexTuple; j < array.length && j <= tupleUpperBound; j++){
                               if (levelKey != array[j][searchDepth]){
                                   tupleUpperBound = j-1;
                                   break;
                               }
                           }
                       }
                   }
               } else {
                   tupleUpperBound = array.length-1;
               }
           }

           //return the complete value of a tuple that was found to be correct.
           public ArrayList<java.lang.Integer> giveResult(){
               ArrayList<java.lang.Integer> myList = new ArrayList<java.lang.Integer>();
               for (int j = 0; j < array[0].length; j++){
                   myList.add(array[indexTuple][j]);
               }
               return myList;
           }

           @Override
           public String debugString(){
               if(indexTuple < array.length && indexDepth >= 0){
                   return "uid: " + uid + ", tLB: " + tupleLowerBound + ", tUB: " + tupleUpperBound + " - iTuple: " + indexTuple +
                           " , iDepth: " + indexDepth + ", key: "+ array[indexTuple][indexDepth];
               } else {
                   return "uid: " + uid + ", tLB: " + tupleLowerBound + ", tUB: " + tupleUpperBound + " - iTuple: " +
                           indexTuple + " , iDepth: " + indexDepth + ", key: Out Of Bound";
               }
           }
       };

        return relIt;
    }
    
}
