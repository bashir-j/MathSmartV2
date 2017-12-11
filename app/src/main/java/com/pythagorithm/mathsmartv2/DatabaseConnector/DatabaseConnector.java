package com.pythagorithm.mathsmartv2.DatabaseConnector;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pythagorithm.mathsmartv2.AppLogic.Assignment;
import com.pythagorithm.mathsmartv2.AppLogic.AssignmentHandler;
import com.pythagorithm.mathsmartv2.AppLogic.AssignmentProgress;
import com.pythagorithm.mathsmartv2.AppLogic.AssignmentReport;
import com.pythagorithm.mathsmartv2.AppLogic.Question;
import com.pythagorithm.mathsmartv2.AppLogic.Student;
import com.pythagorithm.mathsmartv2.AppLogic.Teacher;
import com.pythagorithm.mathsmartv2.UIConnector.UIConnector;
import com.pythagorithm.mathsmartv2.UILayer.assignmentPreview;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by H_Abb on 11/2/2017.
 */

public class DatabaseConnector {
    private String ID;

    private AssignmentHandler assignmentHandler;
    private final String STUDENT_COLLECTION = "STUDENTS";
    private final String COMPLETED_ASSIGNMENTS = "COMPLETED_ASSIGNMENTS";
    private final String ASSIGNMENT_REPORTS = "ASSIGNMENT_REPORTS";


    public DatabaseConnector(String userID){
        this.ID=userID;
    }
    public DatabaseConnector(){}

    public final String QUESTIONS_COLLECTION = "QUESTIONS";
    public final String ASSIGNMENT_COLLECTION="ASSIGNMENTS";
    public final String QUESTION_SCORES_COLLECTION="QUESTION_SCORES";
    public final String ASSIGNMENT_PROGRESS_COLLECTION = "ASSIGNMENT_PROGRESS_COLLECTION";
    public final String TEACHER_COLLECTION = "TEACHERS";

    public DatabaseConnector(AssignmentHandler assignmentHandler){
        this.assignmentHandler = assignmentHandler;
    }

    //=========================================================================================================================
    //OTHER
    //=========================================================================================================================
    public void loginStudent(final UIConnector uic,final String uID){
        Log.d("Firestore", "Initialized getQuestion...");
        FirebaseFirestore.getInstance().collection(STUDENT_COLLECTION)
                .whereEqualTo("studentID", uID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("Firestore","Entered onComplete in loginStudent");
                        if (task.getResult().getDocuments().size()==0){
                            Log.d("Firestore", "Did not find a student with uID"+uID);
                            uic.loginUnsuccessful();
                        }
                        else if (task.isSuccessful()){
                            for (DocumentSnapshot doc : task.getResult()){
                                Log.d("Firestore", "onComplete: "+ doc.getData());
                                Student student= doc.toObject(Student.class);

                                uic.loginSuccessful(student);
                            }
                        }

                    }

                });

    }
    public void loginTeacher(final UIConnector uic, final String uID){
        Log.d("Firestore", "Initialized loginTeacher...");
        FirebaseFirestore.getInstance().collection(TEACHER_COLLECTION)
                .whereEqualTo("teacherID", uID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("Firestore","Entered onComplete in loginTeacher");
                        if (task.getResult().getDocuments().size()==0){
                            Log.d("Firestore", "Did not find a teacher with uID"+uID);
                            uic.loginUnsuccessful();
                        }
                        else if (task.isSuccessful()){
                            for (DocumentSnapshot doc : task.getResult()){
                                Teacher teacher = doc.toObject(Teacher.class);
                                Log.d("Firestore", "onComplete: "+ doc.getData());
                                uic.loginSuccessful(teacher);
                            }
                        }

                    }

                });
    }
    public String getSectionID(String studentID){
        return "";
    }
    public int getTotalQuestionsSolved(String studentID){return 1;}


    //=========================================================================================================================
    //QUESTIONS
    //=========================================================================================================================
    public ArrayList<Question> getAvailableQuestions(String topic, String sectionID){
        return new ArrayList<>();
    }

    private boolean StringIsInArrayList(ArrayList<String> arrayList ,String string){
        for (int i =0; i<arrayList.size();i++){
            if (string.equals(arrayList.get(i)))return true;

        }
        return false;
    }

    public void getQuestion(final ArrayList<String> completedQuestion, final int weight, final String topic){

            Log.d("Firestore", "Initialized getQuestion...");
            FirebaseFirestore.getInstance().collection(QUESTIONS_COLLECTION)
                    //.whereGreaterThanOrEqualTo("weight", (double)weight)
                    .whereEqualTo("topic", topic.substring(0, 1).toUpperCase() + topic.substring(1))
                    .whereEqualTo("weight", weight)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d("Firestore","Entered onComplete in getQuestion");
                            if (task.getResult().getDocuments().size()==0){
                                Log.d("Firestore", "Did not find a question with weight"+weight+". Getting a new questions...");
                                assignmentHandler.getNextQuestion();
                            }
                            else if (task.isSuccessful()){
                                int count = task.getResult().size();
                                for (DocumentSnapshot doc : task.getResult()){
                                    Log.d("Firestore", "Question with ID: "+ doc.getId() +" found. Success.");
                                    Question q = doc.toObject(Question.class);
                                    if (StringIsInArrayList(completedQuestion,q.getQuestionID())){
                                        Log.d("Firestore", "Question with ID: "+ doc.getId() +" found. Not needed.");
                                        if (count==1){
                                            assignmentHandler.getNextQuestion();
                                            Log.d("Firestore","did not find a question with weight"+weight+" and calling getNextQuestion()");
                                        }

                                    }
                                    else {

                                       // if (q.getWeight()==weight){
                                            assignmentHandler.setCurrentQuestion(q);
                                            Log.d("Firestore","set question with weight = "+weight);
                                            return;
                                     //   }
                                        //    Log.d("Firestore", "onComplete: "+ doc.getData());
                                        //====================================================================
                                        // Function that gets called in the AssignmentHandler class
                                        //====================================================================

                                    }
                                    count--;
                                }
                            }

                        }

                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Firestore", "Listener to get question failed");
                }
            });

    }

    public void addQuestion(final Question q){
    Log.d("Hussu","Calling Add Question");
        FirebaseFirestore.getInstance().collection(QUESTIONS_COLLECTION)
                .add(q)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firestore", "added question successfully");
                        Log.d("Hussu", "added question successfully "+documentReference.getId());
                        UIConnector.addedQuestion();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firestore", "Question was not added successfully");
                        Log.d("Hussu", "Question was not added successfully");
                    }
        });
    }
    //=========================================================================================================================
    //ASSIGNMENTS
    //=========================================================================================================================
    public void getAvailableAssignments(final Student student){
        FirebaseFirestore.getInstance()
                .collection(ASSIGNMENT_COLLECTION)
                .whereEqualTo("sectionList."+student.getSectionID(),true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Assignment> assignmentList = new ArrayList<Assignment>();
                        Log.d("Firestore","Entered onComplete to find assignments for student with SID: "+student.getStudentID());
                        if (task.getResult().getDocuments().size()==0){
                            Log.d("Firestore", "Did not find assignments with section: "+student.getSectionID());
                        }
                        for (DocumentSnapshot doc : task.getResult()){
                            Assignment a = doc.toObject(Assignment.class);
                            assignmentList.add(a);
                            Log.d("Firestore", "found assignment AID:"+a.getAssignmentID()+". Adding to list");
                        }
                        try {
                            student.setAssignmentList(assignmentList);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void updateOverallScore(final String studentID, final int overallScore){
        FirebaseFirestore.getInstance()
                .collection(STUDENT_COLLECTION)
                .whereEqualTo("studentID", studentID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        FirebaseFirestore.getInstance()
                                .collection(STUDENT_COLLECTION)
                                .document(doc.getId())
                                .update("overallScore",overallScore)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d("Firestore", "Updated the overallscore of student "+ doc.getId()+" to "+overallScore);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Firestore", "could not update the overallscore of student "+ doc.getId()+" to "+overallScore);
                                    }
                        });
                    }
                });
    }

    public void completeAssignment(final String studentID, final String assignmentID, final AssignmentReport assignmentReport){
        final HashMap<String,Boolean> compAssignment=new HashMap<>();
        compAssignment.put(assignmentID,true);
        FirebaseFirestore.getInstance().collection(STUDENT_COLLECTION)
                .whereEqualTo("studentID", studentID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        FirebaseFirestore.getInstance()
                                .collection(STUDENT_COLLECTION)
                                .document(doc.getId())
                                .collection(COMPLETED_ASSIGNMENTS)
                                .add(compAssignment)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        Log.d("Firestore","Completed assignment "+assignmentID+" added to "+studentID);
                                    }
                                });
                    }
                });
        FirebaseFirestore.getInstance().collection(ASSIGNMENT_REPORTS)
                .add(assignmentReport)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firestore", "added a score report for assignment "+assignmentReport.getAssignmentID());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error writing error "+assignmentReport.getAssignmentID()+ " ", e);
                    }
                });
    }

    public void getCompletedAssignments(final Student student){
        FirebaseFirestore.getInstance()
                .collection(STUDENT_COLLECTION)
                .whereEqualTo("studentID",student.getStudentID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        FirebaseFirestore.getInstance()
                                .collection(STUDENT_COLLECTION)
                                .document(doc.getId())
                                .collection(COMPLETED_ASSIGNMENTS)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        ArrayList<String> assignmentIds = new ArrayList<String>();
                                        Log.d("Firestore","Entered onComplete to find completed assignments for student with SID: "+student.getStudentID());
                                        if (task.getResult().getDocuments().size()==0){
                                            Log.d("Firestore", "Did not find any completed assignments for student "+student.getStudentID());
                                        }
                                        else
                                        for (DocumentSnapshot doc : task.getResult()){
                                            String json = doc.getData().toString();
                                            int eqChar = json.indexOf('=');
                                            String id = json.substring(1, eqChar);
                                            assignmentIds.add(id);
                                            Log.d("Firestore", "found complete assignment AID: " +id);
                                        }
                                        student.setCompletedAssignments(assignmentIds);
                                        getAvailableAssignments(student);
                                    }
                }               );

                    }
                });
    }

    public void getAvailableAssignments(final Teacher teacher, final String sectionID){
        Log.d("Hussu","Entering availAssignments "+sectionID);
        FirebaseFirestore.getInstance()
                .collection(ASSIGNMENT_COLLECTION)
                .whereEqualTo("sectionList."+sectionID,true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Assignment> assignmentList = new ArrayList<Assignment>();
                        Log.d("Firestore","Entered onComplete to find assignments for teacher with TID: "+teacher.getTeacherID());
                        if (task.getResult().getDocuments().size()==0){
                            Log.d("Firestore", "Did not find assignments with section: "+sectionID);
                        }
                        for (DocumentSnapshot doc : task.getResult()){
                            Assignment a = doc.toObject(Assignment.class);
                            assignmentList.add(a);
                            Log.d("Firestore", "found assignment AID:"+a.getAssignmentID()+". Adding to list");
                        }
                        teacher.setAvailableAssignmentsFrom(assignmentList);
                    }
                });
    }

    public void addAssignment(final Assignment a){
        FirebaseFirestore.getInstance().collection(ASSIGNMENT_COLLECTION)
                .add(a)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firestore", "added assignment successfully");
                        Log.d("Hussu", "added assignment successfully");
                        UIConnector.addedAssignment();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firestore", "assignment was not added successfully");
                Log.d("Hussu", "assignment was not added successfully");
            }
        });
    }
    public void getAssignmentProgress(final Student s, final String studentID, final String aID, final assignmentPreview assignmentPreview){
        //Change values of completedQuestions, assignmentScore, and min
        //If not available, change value of completedQuestions to 'null'
        FirebaseFirestore.getInstance()
                .collection(ASSIGNMENT_PROGRESS_COLLECTION)
                .whereEqualTo("assignmentID",aID)
                .whereEqualTo("studentID",studentID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Assignment> assignmentList = new ArrayList<Assignment>();
                        Log.d("Firestore","Entered onComplete to get assignment progress");
                        if (task.getResult().getDocuments().size()==0){
                            Log.d("Firestore", "Did not find assignment progress for assignment "+aID+" for student: "+studentID);
                            assignmentPreview.showButton(null);
                        }
                        for (DocumentSnapshot doc : task.getResult()){
                            Log.d("Firestore", "found progress for student with studentID: "+studentID+" for assignment "+ aID);
                            assignmentPreview.showButton(doc.toObject(AssignmentProgress.class));
                        }

                    }
                });
    }
    /*
        Description: Save progress of an assignment to the database. Can be retrieved later.
        Precondition: a student is logged in and an assignment has been started.
        Postcondition: an assignment progress object is added to the database.
     */
    public void saveAssignmentProgress(final String studentID, final String aID, final ArrayList<String> completedQuestions, final double assignmentScore, int questionsLeft, int questionsAttempted){
        HashMap<String, Boolean> compQs = new HashMap<>();
        for (int i =0; i< completedQuestions.size();i++)
            compQs.put(completedQuestions.get(i),true);
        final AssignmentProgress ap = new AssignmentProgress(studentID, aID,compQs, assignmentScore,questionsLeft, questionsAttempted);
        FirebaseFirestore.getInstance().collection(ASSIGNMENT_PROGRESS_COLLECTION)
                .document(aID)
                .set(ap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Firestore","updated assignment progress of assignment: "+aID);
                    }
                });
    }

    public void createAssignmentProgress(final String studentID, final String aID, final ArrayList<String> completedQuestions , int questionsLeft, int questionsAttempted){
        final AssignmentProgress ap = new AssignmentProgress(studentID, aID,null, 0,questionsLeft, questionsAttempted);
        FirebaseFirestore.getInstance().collection(ASSIGNMENT_PROGRESS_COLLECTION)
                .document(aID)
                .set(ap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Firestore","created assignment progress for assignment: "+aID);
                    }
                });
    }

    //=========================================================================================================================
    //SCORES
    //=========================================================================================================================

    /*
        Description: Create a  new score report for a solved question and updates the assignement score of that solved question
        Precondition: question has been solved.
        Postcondition: a new score for a question is added to the database. The assignment report of the assignment with assignment ID assignmentID
        is either created or updated with the new score
     */
    public void updateScore(String studentID, final String questionID, String assignmentID,ArrayList<String> completedQuestions, double questionScore, double assignementScore, int questionsLeft ,boolean correct, int time, String topic, int difficulty, int questionsAttempted){
        QuestionScore qs = new QuestionScore(studentID, questionID, assignmentID, correct, time, topic, difficulty, questionScore);
        FirebaseFirestore.getInstance().collection(QUESTION_SCORES_COLLECTION)
                .add(qs)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firestore", "Score of question" +questionID+ " saved ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error writing score", e);
                    }
        });
        saveAssignmentProgress(studentID,assignmentID,completedQuestions,assignementScore,questionsLeft, questionsAttempted);

    }

    public void getTopicScores(HashMap<String,Boolean>sectionList){
        HashMap<Integer,String> map = new HashMap<>();
        map.put(0,"Algebra");
        map.put(1,"Multiplication");
        map.put(2, "Addition");
        //
        Log.d("Hussu","Entering getTopicScores dc");

        HashMap<String,Float> map2 = new HashMap<>();
        map2.put("Algebra",7.86f);
        map2.put("Multiplication",8.97f);
        map2.put("Addition",6.99f);
        UIConnector.showBarChartTeacher(map,map2);
    }
    public void getAssignmentsCreated(String teacherID){
        HashMap<String,Integer>map3=new HashMap<>();
        map3.put("Algebra",4);
        map3.put("Addition",2);
        map3.put("Multiplication",4);

        Log.d("Hussu","Entering getAssignmentsCreated dc");
        UIConnector.showPieChartTeacher(map3);

    }
    public void getAssignmentsCompletedScores(String studentID){
        HashMap<String,Integer> map = new HashMap<>();
        map.put("Algebra",3);
        map.put("Multiplication",1);
        map.put("Addition",1);
        UIConnector.showPieChartStudent(map);
    }
    public void getAvgTime(String studentID){
        HashMap<String,Float> map2 = new HashMap<>();
        map2.put("Algebra",5.0f);
        map2.put("Multiplication",10.0f);
        map2.put("Addition",6.0f);
        UIConnector.showBarChartStudent(map2);
    }
    public double getAssignmentScore(String aID,String studentID){
        return 0;
    }
    public double getOverallScore(String studentID){
        return 0;
    }
}
