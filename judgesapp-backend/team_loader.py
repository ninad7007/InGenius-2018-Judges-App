import mysql.connector
import csv

host = "xxx-xxx"
user = "ingenius2018"
passwd = "Pesitbsc"
database = "ingenius2018"
mydb = mysql.connector.connect(host=host, user=user, passwd=passwd, database=database )
mycursor = mydb.cursor()

statement = "SELECT * FROM domain"
mycursor.execute(statement)
domains = mycursor.fetchall()
domains_dict = {}
for row in domains:
    domains_dict[row[1]] = row[0]
print(domains_dict)


statement = "INSERT INTO team (Team_id, Team_name, Seminar_hall, Domain_one_id, Domain_two_id, Primary_judge_id, Secondary_judge_id) VALUES "
with open("after_midnight_teams.csv") as f:
    reader = csv.reader(f, delimiter = ',')
    # data = []
    count = 0
    for row in list(reader):
        ti = row[0]
        tn = row[1]
        pd = row[3]
        sd = row[4]
        sem_hall = row[2]
        pji = row[5]
        sji = row[6]
        count +=1
        # data.append([tn, pd, sd])
        statement += '('+str(ti)+', "'+tn+ '",' +str(sem_hall)+ ' , '+str(domains_dict[pd])+' , '+str(domains_dict[sd])+', '+str(pji)+', '+str(sji)+'),' 
    statement = statement[:-1]+";"
    print(statement)
    mycursor.execute(statement)
    mydb.commit()
    
    mycursor.close()
    mydb.close()
    
