package com.normsstuff.uscg_questions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

import android.os.AsyncTask;
import android.os.Bundle;
//import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Gravity;
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
//import android.R.layout.*;
import android.widget.TextView;

public class USCG_QuestionsActivity extends ListActivity {
	final String Version = "Version date: August 4, 2013\n";
	
	ArrayAdapter<String> adapter;
	int originalClr; // = getResources().getColor(R.color.answer_background);
	int wrongAnswerClr; // = getResources().getColor(R.color.wrong_answer);
	int rightAnswerClr; // = getResources().getColor(R.color.right_answer);
	
	final String Randomize_B = "Randomize";
	final String BookNbr_B = "BookNbr";
	final String QuesNbr_B = "QuesNbr";
	public static final String PREFS_NAME = "USCG_Ques_file";
	
	final String IllusFolder = "images/";
    final String IllusExt = ".jpg";    // image file extension
    final float ImageResizeFactor = 1.3F;  // resize image for old Nexus7  ???
	
	View lastSelection = null;  // used to restore color
	
	// Define structures to hold questions and answers
	final String NoIllusFlag = "<MT>";

    // Define a class to hold the data for each question
    class QuestionData {
      String quesNbr;
      String answer;
      String question;
      String[] choices;
      String bookNbr;
      String illustrations;

      public String toString() {
        return "QD quesNbr="+quesNbr +" choices="+Arrays.toString(choices);
      }
    } // end class

    ArrayList<QuestionData> theQuestions;	 
    boolean[] shownQues;                // true when question has been shown before
    
    float pxlDensity = 1.0F;   // used to unlarge image to fit screen density

   
   int bookNbrIx = 1;                   // Current book
   int thisQues = 0;                    // Current question being displayed
   int lastQues = -1;                   // max number of questions

   Random random = new Random();
   boolean randomizeDisplay = false;



   // List of books and titles - KEEP in Synch!
   final String[] Titles = {"",         // Make table 1 based
                           "Rules of the Road (Book 1)",
                           "General Deck Questions (Book 2)",
                           "General Navigation Questions (Book 3)",
                           "Safety (Book 4)",
                           "Navigation Problems (Book 5)",
//	                           "New and revised questions"
                           };
   final String[] BookFNs = {"",       // one based
                             "Book1_June05.txt",
                             "Book2_June05.txt",
                             "Book3_June05.txt",
                             "Book4_June05.txt",
                             "Book5_June05.txt",
//	                             "NandR_June'05.txt"
                            };
	   
    //------------------------------------------------
	// Define a class to read in the questions
	class ReadQuestions extends AsyncTask<String, Integer, Long> {
		   protected void onPreExecute(String...fns){
			   USCG_QuestionsActivity.this.setContentView(R.layout.loading_screen);
		   }
		   
		   protected Long doInBackground(String... fns) {
			   loadFile(fns[0]);
			   return 0L;
		   }
		   protected void onPostExecute(Long result) {
//		         showMsg("onPostExecute");
			   // Load the right screen
			   USCG_QuestionsActivity.this.setContentView(R.layout.activity_uscg__questions);
		       // Show the question and answers
			   showQuesAndAnswers();
		  }
	}   // end class
	
	PopupWindow puw = null;
	
	//-----------------------------------------------------   
	//  Show the question and list of possible answers
	private void showQuesAndAnswers() {
        QuestionData qd = theQuestions.get(thisQues);
        TextView tv = (TextView)findViewById(R.id.theQuestion);
        tv.setText(Titles[bookNbrIx] + ", question nbr: " + qd.quesNbr
                          + ", line: " + thisQues + "\n\n" + qd.question);
        
		adapter = new ArrayAdapter<String>(USCG_QuestionsActivity.this, android.R.layout.simple_list_item_1, qd.choices);
		setListAdapter(adapter);
		
		// Does this question have an illustration image?
		if(!qd.illustrations.equals(NoIllusFlag)){
			System.out.println("Question "+thisQues+" has diagram:"+qd.illustrations);
			Bitmap bitmap = loadDiagram(IllusFolder + qd.illustrations + IllusExt);
			if(bitmap == null) {
				System.out.println("bitmap null for " + IllusFolder + qd.illustrations + IllusExt);
				showMsg("bitmap null for " + IllusFolder + qd.illustrations + IllusExt);
				return;
			}
			// Build and show a popup window
		    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    int puwWidth = (int)(bitmap.getWidth() * pxlDensity) + 5;
		    int puwHeight = (int)(bitmap.getHeight() * pxlDensity) + 5;
    	    puw = new PopupWindow(inflater.inflate(R.layout.diagram_popup, null, false), 
						    	       puwWidth, 
						    	       puwHeight, 
						    	       true);
//    	    puw.setOutsideTouchable(true);    //WHAT DOES THIS CHANGE ???? <<<<<<<<
    	    puw.setBackgroundDrawable(new BitmapDrawable());  // To enable Back button

     	    puw.showAtLocation(this.findViewById(R.id.theQuestion), Gravity.CENTER, 0, 0); 
     	    puw.setOnDismissListener(new PopupWindow.OnDismissListener(){
     	    	public void onDismiss() {
     	    		puw = null;  // set flag for onBackPressed()
     	    	}
     	    }  	    
     	    );
     	    
     	    // Show the image in a popup window
     	    ShowDiagramView sdv = (ShowDiagramView)puw.getContentView().findViewById(R.id.view_area);
     	    sdv.setBitmap(bitmap, puw, pxlDensity);
     	    sdv.invalidate();  // force a call to onDraw()
     	    
//       	  TextView tv2 = (TextView)puw.getContentView().findViewById(R.id.view_area);
//    	      tv2.setText("Question has diagram:"+qd.illustrations);
   	    
    	    Button dspBtn = (Button)puw.getContentView().findViewById(R.id.disposeBtn);
            dspBtn.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                    puw.dismiss();
                    puw = null;   // flag for onBackPressed()
                }
            });
// */
		}

	}
	
	// Load an image
	public Bitmap loadDiagram(String fn) {
    	AssetManager am = getAssets();
    	Bitmap bitmap = null;
    	try {
    		InputStream is = am.open(fn);
    		bitmap = BitmapFactory.decodeStream(is);
    		System.out.println("loadDiagram bitmap size w="+bitmap.getWidth() + ", h="+bitmap.getHeight());
    	}catch(Exception x){
    		x.printStackTrace();
    	}
    	return bitmap;
	}

	@Override
	public void onBackPressed() {
      System.out.println("backPressed puw="+puw);
      if(puw != null)  {
           puw.dismiss();  
           puw = null;
       }  else { 
           super.onBackPressed();  
        }  
	}


	//---------------------------------------------------------------------
	// Start the activity here
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_screen);
/*		
		setContentView(R.layout.activity_uscg__questions);
		
		// Define some testing answers
		String[] answers = {"Answer 1", "Answer 2 is here", 
				    "This is the third Answer and it will be longer and span a line."
					+ "This will have to be long enough to do it. So we'll keep typing until it is long enough",
				    "The last answer here"};
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, answers);
		setListAdapter(adapter);
*/		
		// Have to do this here vs at the definitions
		originalClr = getResources().getColor(R.color.answer_background);
		wrongAnswerClr = getResources().getColor(R.color.wrong_answer);
		rightAnswerClr = getResources().getColor(R.color.right_answer);
		
		pxlDensity = getResources().getDisplayMetrics().density / ImageResizeFactor;
		
		// Check if we've been restarted and go to where we left off
		if(savedInstanceState  != null) {
			randomizeDisplay = savedInstanceState.getBoolean(Randomize_B);
			bookNbrIx = savedInstanceState.getInt(BookNbr_B);
			thisQues = savedInstanceState.getInt(QuesNbr_B);
		}

		new ReadQuestions().execute(BookFNs[bookNbrIx]); // read the first book
		
		// DEBUG
//		Resources res = getResources();
//		float QAheight = res.getDimension(R.dimen.question_area_height);
//		showMsg("QAheight="+QAheight);  //=100.0

	}
	
	//--------------------------------------------------------------
	//  Save values to allow us to restore state when rotated
	@Override
	public void onSaveInstanceState(Bundle bndl) {
		super.onSaveInstanceState(bndl);
		bndl.putBoolean(Randomize_B, randomizeDisplay);
		bndl.putInt(BookNbr_B, bookNbrIx); 
		bndl.putInt(QuesNbr_B, thisQues);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.uscg__questions, menu);
		return true;
	}
	
	//----------------------------------------------------------
	// Handle menu item selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("oOIS", "item="+item.getItemId());
	    // Handle item selection
	    switch (item.getItemId()) {
	        // Load questions from one of the books
	    	case R.id.book1:
	    		bookNbrIx = 1;
	    		new ReadQuestions().execute(BookFNs[1]); // read the first book
	    		thisQues = 0; // reset
	    		return true;
	    	case R.id.book2:
	    		bookNbrIx = 2;
	    		new ReadQuestions().execute(BookFNs[2]); // read the second book
	    		thisQues = 0;  // reset
	    		return true;
	    	case R.id.book3:
	    		bookNbrIx = 3;
	    		new ReadQuestions().execute(BookFNs[3]); // read the third book
	    		thisQues = 0;
	    		return true;
	    		
	    	case R.id.tofirstques:
	    		thisQues = 0;
	    		nextQuestion(0);
	    		showQuesAndAnswers();
	    		return true;
	    		
	    	case R.id.gotoquestion:
	        	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        	alert.setTitle("Go to question");
	        	alert.setMessage("Enter number of question to go to:");

	        	// Set an EditText view to get user input 
	        	final EditText input = new EditText(this);
	        	input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);  //  numbers only
	        	alert.setView(input);

	        	alert.setPositiveButton("Go to question", new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int whichButton) {
		        	  String fndStr = input.getText().toString();
		        	  if(fndStr == null || fndStr.length() == 0)
		        		  return;		// exit if user quit
		        	    // Position to the desired question
//	        	        showMsg("go to question:"+fndStr);
	        	        for(int i=0; i < theQuestions.size(); i++) {
	        	            if(theQuestions.get(i).quesNbr.equals(fndStr)) {
	        	               thisQues = i;        // set the global to this record
	        	               nextQuestion(0);
	        	               showQuesAndAnswers();
	        	               return;
	        	            }
	        	         } // end for(i)

	        	       showMsg("Question: "+ fndStr + " not found.");
	        	    }
	        	});

	        	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        	   public void onClick(DialogInterface dialog, int whichButton) {
	        	    // Canceled.
	        	   }
	        	});

	        	alert.show();

	    		return true;
	    		
	        case R.id.randomize:
	        	//  toggle setting
	        	randomizeDisplay = !item.isChecked();
	        	if(item.isChecked())
	        		item.setChecked(false);
	        	else
	        		item.setChecked(true);
	        	return true;
	        	
	        	//  Save book number and question number 
	        case R.id.save_position:
//	        	showMsg("Saveing bkNbr="+bookNbrIx +" and ques nbr="+thisQues);
	            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	            SharedPreferences.Editor editor = settings.edit();
	            editor.putInt(BookNbr_B, bookNbrIx);
	            editor.putInt(QuesNbr_B, thisQues);

	            // Commit the edits!
	            editor.commit();
	        	return true;
	        	
	        	//  Resume at saved book and question
	        case R.id.resume:
	        	SharedPreferences settingsR = getSharedPreferences(PREFS_NAME, 0);
	            int bookNbr = settingsR.getInt(BookNbr_B, 1);
	            thisQues = settingsR.getInt(QuesNbr_B, 0);
	            if(bookNbr != bookNbrIx) {
	            	bookNbrIx = bookNbr;  // set new book and go read it
	            	new ReadQuestions().execute(BookFNs[bookNbrIx]); // read the required book	
	            }
	            showQuesAndAnswers();
	        	return true;
	        	
	        case R.id.about:
	            showMsg("Norm's Coast Guard questions\n"
	            		+ Version
	            		+ "email: radder@hotmail.com");
	            return true;
	            
	        case R.id.exit:
	        	finish();
	        	return true;
	        	
	        default:
	            return super.onOptionsItemSelected(item);
	    }  // end select()
	}

	//--------------------------------------------------------------------
	//  Set color of background of selected item to show if right/wrong
	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id){
        QuestionData qd = theQuestions.get(thisQues);
        String text = qd.choices[position];
//		showMsg("ListItem " + position + ", selected ans="+ text + ", correct ans="+qd.answer);

		if(lastSelection != null) {
			lastSelection.setBackgroundColor(originalClr);
		}
		//  The first letter in the text is the answer id: A,B,C,D
        if(text.startsWith(qd.answer))
			v.setBackgroundColor(rightAnswerClr);
        else
			v.setBackgroundColor(wrongAnswerClr);
	
		lastSelection = v;   // save to be able to restore the color
	}
	
	//----------------------------------------------
	// Handle button clicks here
	public void nextBtnClicked(View v) {
//		showMsg("Next Button clicked: "+ v);
		resetSelection();
		nextQuestion(1);
		showQuesAndAnswers();
	}

	public void previousBtnClicked(View v){
//		showMsg("Previous Button clicked: "+ v);
		resetSelection();
		nextQuestion(-1);
		showQuesAndAnswers();
	}
	
	private void resetSelection() {
		if(lastSelection != null) {
			lastSelection.setBackgroundColor(originalClr);
		}
		lastSelection = null;
	}
    //--------------------------------------------------
    private void nextQuestion(int bump) {
      // First determine which question to show and set thisQues
      if(bump < 0) {
         thisQues--;
         if(thisQues < 0)
            thisQues = lastQues;       // wrap

	      }else if(randomizeDisplay) {
	         thisQues = random.nextInt(lastQues);
	         while(shownQues[thisQues]){
	            thisQues++;
	            if(thisQues > lastQues)
	               thisQues = 0;           // wrap
	         }

      }else {
         if(bump > 0)
            thisQues++;
         if(thisQues > lastQues)
            thisQues = 0;              // wrap
      }
    }
	   
	//------------------------------------------------------------------
	//  Show a message in an Alert box
	private void showMsg(String msg) {

		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(msg);
		ad.setButton(DialogInterface.BUTTON_POSITIVE, "Clear messsge", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();                    
		    }
		});
		ad.show();
	}
	

   //--------------------------------------------------------------------
   private void loadFile(String fn) {
      System.out.println("Loading book from file: " + fn);
      String rec = null;
      int cnt = 0;
      try {
    	 AssetManager am = getAssets();
  		 InputStream is = am.open(fn);
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
         theQuestions = new ArrayList<QuestionData>();

         rec = br.readLine();          // throw away first line
         //BK,NBR,ANS,QUESTION,CHOICE  A,CHOICE  B,CHOICE  C,CHOICE  D,ILLUSTRATION
         while((rec = br.readLine()) != null) {
            cnt++;                     // Count the record
            if(rec.length() < 2)
               continue;               //ignore short lines
          try {
            StringTokenizer st = new StringTokenizer(rec, "\t");
            String bkNbr = st.nextToken();
            String nbr = st.nextToken();
            String ans = st.nextToken();
            String ques = stripQuotes(st.nextToken());

            // If the question has a leading " and no ending " then we need
            // to read (and append to question) until we find the ending "
            if(ques.startsWith("\"") && !ques.startsWith("\"\"")) {
               if(st.hasMoreTokens())
                  System.out.println("*** More tokens without ending \" ques= " + ques
                                 + "<\n rec=" + rec);
              lp: while(true) {
                  rec = br.readLine();
                  ques += "\n";        // Remember newline
                  st = new StringTokenizer(rec, "\t");
                  while(st.hasMoreTokens()){
                     String tok = st.nextToken();
                     ques += tok;
                     if(tok.endsWith("\""))  {
                        ques = stripQuotes(ques);
                        break lp;      // Found ending "
                     }
                  }
               } // end while(true)
            } // end search for ending "

            String[] ch = new String[4];
            ch[0] = "A). " + stripQuotes(st.nextToken());
            ch[1] = "B). " + stripQuotes(st.nextToken());
            ch[2] = "C). " + stripQuotes(st.nextToken());
            ch[3] = "D). " + stripQuotes(st.nextToken());

            String illus = NoIllusFlag;
            if(st.hasMoreTokens())
               illus = st.nextToken(); 


            // Got them all, now save
            QuestionData qd = new QuestionData();
            qd.quesNbr = nbr;
            qd.question = ques;
            qd.answer = ans;
            qd.choices = ch;
            qd.bookNbr = bkNbr;
            qd.illustrations = illus;
//	            System.out.println("qd="+qd); //<<<<<<<<
            theQuestions.add(qd);
          }catch(Exception ex) {
            System.out.println(ex  + ", cnt=" + cnt + " " + rec);
          }
         } // end reading records

         br.close();

         // Now copy Vectors to String[]
         int aSize = theQuestions.size();
         lastQues = aSize-1;
         System.out.println("loadFile()  read " + cnt + " records, saved " + aSize);
         shownQues = new boolean[aSize];
      }catch(Exception ex) {
         ex.printStackTrace();
         System.out.println("loadFile() cnt=" + cnt + ", rec=" + rec);
      }
   } // end loadFile()

   //----------------------------------------------
   private String stripQuotes(String s) {
      if(s.startsWith("\"") && s.endsWith("\""))
         return s.substring(1, s.length()-1);
      return s;
   } // end stripQuotes()


}
