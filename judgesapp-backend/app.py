import mysql.connector
import csv
from flask import Flask, request, jsonify, send_file

app = Flask(__name__)
host = "xxx-xxx"
user = "ingenius2018"
passwd = "Pesitbsc"
database = "ingenius2018"

@app.route("/")
def root():
    return "You have hit the root route"

@app.route("/getTeams")
def get_teams():
    
    mydb = mysql.connector.connect(host=host, user=user, passwd=passwd, database=database )
    mycursor = mydb.cursor()

    statement = "SELECT * FROM myTeamView" 
    mycursor.execute(statement)
    data = mycursor.fetchall()
    
    mycursor.close()
    mydb.close()
    return jsonify(data)

@app.route("/getJudges")
def get_judges():
    mydb = mysql.connector.connect(host=host, user=user, passwd=passwd, database=database )
    mycursor = mydb.cursor()

    statement = "SELECT * FROM judge" 
    mycursor.execute(statement)
    data = mycursor.fetchall()
    
    mycursor.close()
    mydb.close()
    return jsonify(data)

@app.route("/getScores")
def get_scores():
    mydb = mysql.connector.connect(host=host, user=user, passwd=passwd, database=database )
    mycursor = mydb.cursor()

    statement = "SELECT * FROM myScoreView" 
    mycursor.execute(statement)
    data = mycursor.fetchall()
    
    with open("scores.csv", "w", newline='') as f:
        score_writer = csv.writer(f, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        score_writer.writerow([i[0] for i in mycursor.description])
        for row in list(data):
            score_writer.writerow(row)
    
    mycursor.close()
    mydb.close()

    return send_file("scores.csv", attachment_filename="scores.csv", as_attachment=True)

@app.route("/insertScore", methods=['POST'])
def insert_score():
    try:
        mydb = mysql.connector.connect(host=host, user=user, passwd=passwd, database=database )
        mycursor = mydb.cursor()
        insertion_statement = "INSERT INTO score VALUES (%s, %s, %s, %s, %s, %s)"
        insertion_data = (
                request.form.get("judgeID"), 
                request.form.get("teamID"),
                request.form.get("Technical_Difficulty"),
                request.form.get("Completeness"),
                request.form.get("Originality"),
                request.form.get("Feasibility"),
        )
        mycursor.execute(insertion_statement, insertion_data)
        mydb.commit()
        mycursor.close()
        mydb.close()
        return "SCORE_INSERTED"
    except:
        return "ALREADY_JUDGED"

if __name__ == "__main__":
    app.run()
