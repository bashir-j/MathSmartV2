package com.pythagorithm.mathsmartv2.AppLogic;

import android.util.Log;

import com.pythagorithm.mathsmartv2.DatabaseConnector.DatabaseConnector;
import com.pythagorithm.mathsmartv2.UILayer.LoginActivity;

import java.util.ArrayList;

/**
 * Created by H_Abb on 11/2/2017.
 */

public class Student extends User {
    private String sectionID;
    private String studentID;
    private ArrayList<Assignment> assignmentList;
    private ArrayList<String>currAssignmentQuestions;
    private int totalQuestionsSolved;
    private double currAssignmentScore;
    private double overallScore;
    private int min;
    private DatabaseConnector dc;
    private AssignmentHandler aH;

    //Constructor
    public Student(String username){
        super(username);
        this.studentID=super.userID;
        this.dc=super.dc;
        this.sectionID=dc.getSectionID(studentID);
        this.overallScore=dc.getOverallScore(studentID);
        this.assignmentList= new ArrayList<Assignment>();
        this.totalQuestionsSolved=dc.getTotalQuestionsSolved(studentID);
    }

    public Student(){
        sectionID = "sectionA";
        studentID = "5s5R52Ct8mSKN1vndbuOQNlOZI53";
        overallScore=3;
        dc = new DatabaseConnector();
    }

    //getters and setters
    public String getSectionID() {return sectionID;}
    public void setSectionID(String sectionID) {this.sectionID = sectionID;}
    public String getStudentID() {return studentID;}
    public void setStudentID(String studentID) {this.studentID = studentID;}
    public void setAssignmentList(ArrayList<Assignment> assignmentList) {
        this.assignmentList = assignmentList;
        Log.d("Firestore","changed assignment list to new assignment list with "+ assignmentList.size()+ " assignments");
        // UI.showassignments
        LoginActivity.showAssignments();
    }
    public ArrayList<String> getCurrAssignmentQuestions() {return currAssignmentQuestions;}
    public void setCurrAssignmentQuestions(ArrayList<String> currAssignmentQuestions) {this.currAssignmentQuestions = currAssignmentQuestions;}
    public double getCurrAssignmentScore() {return currAssignmentScore;}
    public void setCurrAssignmentScore(double currAssignmentScore) {this.currAssignmentScore = currAssignmentScore;}
    public double getOverallScore() {return overallScore;}
    public void setOverallScore(double overallScore) {this.overallScore = overallScore;}
    public int getMin() {return min;}
    public void setMin(int min) {this.min = min;}
    public AssignmentHandler getaH() {return aH;}
    public void setaH(AssignmentHandler aH) {this.aH = aH;}


    public void fetchAssignmentList() {
        dc.getAvailableAssignments(this, sectionID);
        Log.d("Firestore","Initiated assignment fetching for student SID: "+studentID);
    }

    public void startAssignment(Assignment assignment){

        dc.getAssignmentProgress(this, studentID, assignment.getAssignmentID());
//        if(assignmentNum<assignmentList.size()) {
//            dc.getAssignmentProgress(assignmentList.get(assignmentNum).getAssignmentID(),studentID,currAssignmentQuestions,currAssignmentScore,min);
//            if(currAssignmentQuestions==null) {
//                aH = new AssignmentHandler(assignmentList.get(assignmentNum), studentID,overallScore,totalQuestionsSolved);
//            }
//            else{
//                aH=new AssignmentHandler(assignmentList.get(assignmentNum),studentID,overallScore,currAssignmentQuestions,currAssignmentScore,min,totalQuestionsSolved);
//            }
//            return aH.getCurrentQuestion();
//        }
//        else
//            return null;
    }

    public void createAssignmentHandler(boolean found, AssignmentProgress ap, String aID){
        Assignment a = null;
        for (int i =0;i < assignmentList.size(); i++) {
            if (assignmentList.get(i).getAssignmentID().equals(aID)) {
                a = assignmentList.get(i);
                if (found) {
                    aH = new AssignmentHandler(a, studentID, (double) overallScore, ap.getCompletedQuestions(), ap.getAssignmentScore(), ap.getQuestionsLeft(), 0/*duno lol*/);
                }
                else {
                    // TODO: questions attempted for what exactly
                    aH = new AssignmentHandler(a, studentID,overallScore,0);
                }
                LoginActivity.assignmentHandlerReady(aH);
            }
        }
    }
//    public boolean saveAssignment(){
//        return aH.saveAssignment();
//    }
//    public Question getNextQuestion(Question currQ,int time,boolean answer){
//        return aH.solveQuestion(currQ,time, answer);
//    }
    public double getAssignmentScore(int assignmentNum){
        return dc.getAssignmentScore(assignmentList.get(assignmentNum).getAssignmentID(),studentID);
    }
    public double getOverallScore(String studentID){
        return dc.getOverallScore(studentID);
    }
}
