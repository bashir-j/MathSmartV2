package com.pythagorithm.mathsmartv2.AppLogic;

import android.util.Log;

import com.pythagorithm.mathsmartv2.DatabaseConnector.DatabaseConnector;
import com.pythagorithm.mathsmartv2.UILayer.assignmentQuestion;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by H_Abb on 11/3/2017.
 */

public class AssignmentHandler  {
    /*
    CONSTANTS for calculating score and for defining number of difficulties and versions
     */
    private final double CORRECT_ANSWER_WEIGHT_VALUE=0.96374752;
    private final double CORRECT_ANSWER_TIME_VALUE=-0.0018861;
    private final double CORRECT_ANSWER_COEFF = 0.662951474663;
    private final double INCORRECT_ANSWER_COEFF = 0.028;
    private final double INCORRECT_ANSWER_WEIGHT_VALUE = 0.9609;
    private final int MAX_WEIGHT=10;
    //other attributes
    private int min;
    private String studentID;
    private Assignment assignment;
    private double overallScore;
    private double currentScore;
    private double assignmentScore;
    private int totalQuestionsAttempted;
    private DatabaseConnector dc;
    private Question currentQuestion;
    private boolean questionAvailable;
    private ArrayList<String> completedQuestions;

    private AssignmentReport assignmentReport;

    private int scorePlus;
    private int scoreMinus;
    private boolean alt;
    private int nextQWeight;

    private assignmentQuestion assignmentQuestion;


    /*
        Constructor: Initializes studentID to upload quizScore
         */
    //Starting new Assignment
    public AssignmentHandler(Assignment assignment,String studentID,double overallScore){
        Log.d("Firestore", "Assignment handler for assignment "+assignment.getAssignmentID()+" created for the first time");
        this.studentID=studentID;
        this.assignment=assignment;
        this.currentScore = this.overallScore=overallScore;
        this.min=assignment.getMinCorrectAnswers();
        this.completedQuestions=new ArrayList<>();
        this.assignmentScore=0;
        this.totalQuestionsAttempted=0;
        dc=new DatabaseConnector(this);
        dc.createAssignmentProgress(studentID,assignment.getAssignmentID(),completedQuestions,min, 0);
        start();
    }
    /*
    Continuing an existing assignment
    Extra arguments required: List of completed questions (Only question ID), assignment score, and also the updated minimum number of correct answers
     */
    AssignmentHandler(Assignment assignment,String studentID,double overallScore,ArrayList<String> completedQuestions,double assignmentScore,int min
    ,int totalQuestionsAttempted){
        Log.d("Firestore","Assignment handler for assignment "+assignment.getAssignmentID()+" created to resume");
        this.studentID=studentID;
        this.assignment=assignment;
        this.completedQuestions=completedQuestions;
        this.assignmentScore=assignmentScore;
        this.currentScore = this.overallScore=overallScore;
        this.min=min;
        this.totalQuestionsAttempted=totalQuestionsAttempted;
        dc=new DatabaseConnector(this);
        start();
    }


    //Functions
    /*
    Description: Starts an assignmnet solving process and prepares the database elements related to this assignment
    (progress tracker and next question)

    Precondition: Student user is logged in, student has chosen an assignment, and there is internet connection
    Postcondition:
     -  an assignment progress object for this instance of the assignment by this user is created in
        the database.
     -  Initiated receiving a new question from the database based on the student's level and the assignment
        topic
     */
    private void start(){
        nextQWeight = ceil(overallScore);
        scoreMinus = scorePlus = nextQWeight;
        Log.d("Firestore", "Assignment handler of aissgnment: "+assignment.getAssignmentID()+" started");
        questionAvailable = false;
        dc.getQuestion(completedQuestions,ceil(overallScore),assignment.getAssignmentTopic());
    }
    /*
    Function used to generate current question score and send back new question
    Description: Takes in the time and answer of the user and calculates the question score and adds it the database.
        Also, it will increment the assignment score and either signal ending the assignment or fetches a new question
        to the student


    Preconditions: User selected an answer for currentQuestion
    Postcondition: Creates a new QuestionScore object and updates assignment score. Returns:
        True: signals the ending of the assignment, or
        False: begins fetching next question
     */
    public boolean solveQuestion(int time,boolean answer) {
        Log.d("Hussu","Entering SolveQuestion");
        totalQuestionsAttempted++;
        Log.d("Hussu","Overall score before: "+overallScore );
        overallScore = ((overallScore*totalQuestionsAttempted)+ currentScore)/(totalQuestionsAttempted+1);
        Log.d("Hussu","Overall score after: "+overallScore );
        nextQWeight=ceil(overallScore);
        completedQuestions.add(currentQuestion.getQuestionID());
        scoreMinus = scorePlus = nextQWeight;


        //scores generation
        Log.d("Hussu","Current score before: "+currentScore );
        Log.d("Hussu","Assignment score before: "+assignmentScore );
        currentScore = masterFormula(currentQuestion.getWeight(),answer,time);
        assignmentScore+=currentScore;
        Log.d("Hussu","Current score after: "+currentScore );
        Log.d("Hussu","Assignment score after: "+assignmentScore );
        questionAvailable = false;
        dc.updateScore(studentID, currentQuestion.getQuestionID(), assignment.getAssignmentID(),completedQuestions,currentScore, assignmentScore, min,answer,time,currentQuestion.getTopic(),currentQuestion.getWeight(), totalQuestionsAttempted);

        currentQuestion=null;

        //checking whether assignment is complete of not
        if(answer)
            min--;
        if(min==0) {
            HashMap<String, Boolean> completedQuestions = new HashMap<>();
            for (int i =0; i< this.completedQuestions.size();i++){
                completedQuestions.put(this.completedQuestions.get(i), true);
            }
            assignmentReport = new AssignmentReport(studentID, completedQuestions, assignment.getAssignmentID(), assignment.getAssignmentTopic(),
                    assignmentScore, totalQuestionsAttempted);
            dc.completeAssignment(studentID, assignment.getAssignmentID(),assignmentReport);
            dc.updateOverallScore(studentID,ceil(overallScore));
            return true;
        }
        else {
            getNextQuestion();
            return false;
        }

    }

/*
    Description:    initiates the fetching of an appropriate question from the database
    Precondition:   currentQuestion is null and assignment handler requires a question
    Postcondition:  a question has been requested based on the parameters the assignment
                    handler supplied
 */
    public void getNextQuestion(){
        //getting appropriate question
        Log.d("Firestore", "called getNextQuestion");
        Log.d("Firestore", "Minus: "+scoreMinus+" Plus: "+scorePlus+" nextQ: "+nextQWeight);
//        nextQWeight=ceil(overallScore);

        if(alt)
            nextQWeight=++scorePlus;
        else
            nextQWeight=--scoreMinus;
        alt=!alt;
        if (scoreMinus<1&&scorePlus>10){
            Log.d("Firestore", "No questions for assignment: "+assignment.getAssignmentID()+" found");
            assignmentQuestion.noQuestionsError();
        }
        else dc.getQuestion(completedQuestions, nextQWeight, assignment.getAssignmentTopic());

    }

    /*
    Description: calculates the score of the question solved
    Precondition: solveQuestion has been called on currentQuestion
    Postcondition: returns a double indicating the score of the question solved
     */
    private double masterFormula(int weight, boolean correct,int time){
        if(correct) {
            double score = CORRECT_ANSWER_COEFF + CORRECT_ANSWER_WEIGHT_VALUE * weight + CORRECT_ANSWER_TIME_VALUE * time;
            if (score > 10) return 10;
            else if (score < 1) return 1;
            else return score;
        }
        else
            return Math.max(INCORRECT_ANSWER_COEFF+INCORRECT_ANSWER_WEIGHT_VALUE*weight,10);
    }

    /*
    Description: returns the cieling of a double as an integer
    Precondition: supplied a double number
    Postcondition: returns the number rounded up to the closes integer
     */
    private int ceil(double score){
        return (int)Math.ceil(score);
    }

    //setters and getters
    public Assignment getAssignment() {return assignment;}
    public void setAssignment(Assignment assignment) {this.assignment = assignment;}
    public double getOverallScore() {return overallScore;}
    public void setOverallScore(double overallScore) {this.overallScore = overallScore;}
    public double getCurrentScore() {return currentScore;}
    public void setCurrentScore(double currentScore) {this.currentScore = currentScore;}
    public Question getCurrentQuestion() {return currentQuestion;}
    public void setCurrentQuestion(Question currentQuestion) {
        questionAvailable = true;
        this.currentQuestion = currentQuestion;
        Log.d("Firestore", "Setting current question");
        assignmentQuestion.setQuestion();
    }
    public ArrayList<String> getCompletedQuestions() {return completedQuestions;}
    public void setCompletedQuestions(ArrayList<String> completedQuestions) {this.completedQuestions = completedQuestions;}
    public double getAssignmentScore() {return assignmentScore;}
    public void setAssignmentScore(double assignmentScore) {this.assignmentScore = assignmentScore;}
    public AssignmentReport getAssignmentReport() {return assignmentReport;}
    public com.pythagorithm.mathsmartv2.UILayer.assignmentQuestion getAssignmentQuestion() {return assignmentQuestion;}
    public void setAssignmentQuestion( assignmentQuestion assignmentQuestion) {this.assignmentQuestion = assignmentQuestion;}

    public int getTotalQuestionsAttempted() {
        return totalQuestionsAttempted;
    }

    public void setTotalQuestionsAttempted(int totalQuestionsAttempted) {
        this.totalQuestionsAttempted = totalQuestionsAttempted;
    }
}
