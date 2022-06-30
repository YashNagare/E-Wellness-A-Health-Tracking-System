from flask import Flask, render_template, request, redirect, url_for
import firebase_admin
from firebase_admin import credentials, firestore

app = Flask(__name__)

json_data = {
    "type": "service_account",
    "project_id": "e-wellness-6a185",
    "private_key_id": "4f7219581490254e291d5b98b13a9c6ffc7e0c1d",
    "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCd1nHlgSzBlPb+\nZYIYPsAvP2juiyqhOYRD6tvn5bC4X0xtUAGCzQ0FYw45LN6WqSzo13r2avtvQINg\n+kpDU8pX2HkifBNbH6p0I+GqPXGRqzdwxtdBcw6/DXvKThJB5nVzzdetiQJ26oHt\nyk/fv+RDeKhbvN+ZEpPXkP73cmgf/5ffHt638lm8w/kifoqDofnHND8IkgHhnGFY\nVhouKt+1hPvLIwHh9ba/A+4uqJuOYYJmPHw5VKO0LohA9lFOD5b7BA/JbYi7kE7F\n8EwimRTMunfPad5u/mBAPjPFyJvP4a0n7EQKqBnXMTa1eR6k+GXT5yrTr7Nn4M4c\nV8VJtcGjAgMBAAECggEADsMA9qdUzutmXkySv5RpglcP3Uouu9LBSAjYdaEfHLSX\nqpw88prIM6TGrm+VMcoIuo4h9eyeL2gn4Wix4VzIzoZ0bdaPiN+L9ZNq250mLgAx\nYzQgQFgeckffOACCkfSX2jN6OcStR6nkakpC8M467wSDL8aZCEabL8y5ITNrVvKk\nVFuu63XSN1tFNIfuM97n8/KhBYksVbQK3rlZHUxxJCSWgUbudAyN/B+Dn6O/IziX\nnJDyOolzW+l1d75uAiJQd2k8v8nZtohv53MkAtfM00+Y0PPSheQEPAcPU6fFOvOd\nPJwE4u9wbfC8BxFGB16PPkNC8/ojyA6WIcyXKAKPQQKBgQDTtQYlhDtKTVxEMquc\nnLve9Ohao5JxN0n+SmUf7DtcFuu0kkvd8UfLtaPaL8gI94RHbYKIbI0WGcWA7Fa6\nkcfOuLXn9+hpqE7Cmgs7lF0K3obHUCueCNsjFpRBJtNSwascgNYvpWYs7Y7WkEsd\nS/0t3GSP0PXLZjnMbjWniC3WOwKBgQC+3DAWSxLD0ROJuxhevDH1uaFHH5MPvNmv\nnzk2WKWuQKK+/Fu+9rwzZ0JZ3USHYiNyVVAji+pyQkbjwEYGM0XygpwrHe1rPbZV\n/cTX8u1dY5NbYdAfIxgRcghd3/v8l8i9TjXtPAjQybtaauct7V3YXvx085bG5DSk\ncQpoocXDuQKBgQCbswwFNX4BhGmyQm3SLbdCH25vktNV7qq9mWtMEgoKDVVmpR2k\n6hm6aSlLg06jwZ4IhjK1MNiGg4es7KRQbHy9vT0TkFDevAjIuFBAjjnrj4MiPQUH\nIk5APQ/l5q8Osx6sRNjBi+xcaI5foRmNP2DBhNB0sOLEp7ckcX2Ga/ICFwKBgCHa\n2ujJbk2vw4B14I/FYDNPNy1vi12KSS7e1n4uYU00h+nKx9dq2t1fXqSkHCjLnrAE\nNA/qHK8h1INHsuQcqjanNl5rk5anPrWoKweHPtWG+TeHdSxgxD8r9BH6zi7zGZeC\nzQwEzyeB8SrLtbAu36umxa9VGhSznCbjKk5/C555AoGAFkMxLBkoCF0iZfTxGLXx\nvVwBdpU0QqjPzPxHMNwbOlvaVYwQ9KUqZVSD70H/MA6PDhRazNd5WgCpaRrAJwnz\nbGWnl67mhd2DcrOG9ChJ2BXTZTFX03akBVAOv05Ib0T912y8MVCmKywctMOSHba1\nuxfYUvgpMlieh6ro3XJXpSw=\n-----END PRIVATE KEY-----\n",
    "client_email": "firebase-adminsdk-6ggkm@e-wellness-6a185.iam.gserviceaccount.com",
    "client_id": "104538795858098170589",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-6ggkm%40e-wellness-6a185.iam.gserviceaccount.com"
}

cred = credentials.Certificate(json_data)
firebase_admin.initialize_app(cred)
fStore = firestore.client()

@app.route("/", methods = ['POST', 'GET'])
def pie():
    if request.method == 'POST':
        user = request.form["nm"]
        return redirect(url_for('User_Details', name=user))

    document_ids = []

    collections = fStore.collection("Users")
    for docs in collections.list_documents():
        document_ids.append(docs.id)

    insert_doc_data = []
    for ids in document_ids:
        collections = fStore.collection('Users').document(ids).collections()
        for collection in collections:
            for doc in collection.stream():
                temp = doc.to_dict()
                insert_doc_data.append(temp)
    i = 0
    diabetic_peoples = 0
    while (len(insert_doc_data) > i):
        if insert_doc_data[i]['Diabetes'] == 0:
            diabetic_peoples += 1
        i += 3

    labels = ['Non-Diabetic', 'Diabetic']
    values = [diabetic_peoples, len(document_ids) - diabetic_peoples]

    colors = ["#F7464A", "#46BFBD"]
    chd_colors = ["#58508d", "#ffa600"]
    asthma_colors = ["#FF7300", "#7CDDDD"]

    diabetes_f_l_u_name = []

    i = 0
    while (len(insert_doc_data) > i):
        if (insert_doc_data[i]['Diabetes'] == 1):
            fname = insert_doc_data[i + 1]['FirstName']
            lname = insert_doc_data[i + 1]['LastName']
            uname = insert_doc_data[i + 1]['UserName']
            diabetes_f_l_u_name.append(fname + ' ' + lname + '  -  ' + uname)
        i += 3

    # CHD Analysis
    i = 0

    chd_peoples = 0
    while (len(insert_doc_data) > i):
        if insert_doc_data[i]['CHD'] == 0:
            chd_peoples += 1
        i += 3

    chd_labels = ['Non-CHD', 'CHD']
    chd_values = [chd_peoples, len(document_ids) - chd_peoples]

    chd_f_l_u_name = []

    i = 0
    while (len(insert_doc_data) > i):
        if (insert_doc_data[i]['CHD'] == 1):
            fname = insert_doc_data[i + 1]['FirstName']
            lname = insert_doc_data[i + 1]['LastName']
            uname = insert_doc_data[i + 1]['UserName']
            chd_f_l_u_name.append(fname + ' ' + lname + '  -  ' + uname)
        i += 3


    # Asthma Analysis

    i = 0

    asthma_peoples = 0
    while (len(insert_doc_data) > i):
        if insert_doc_data[i]['Asthma'] == 0:
            asthma_peoples += 1
        i += 3

    asthma_labels = ['Non-Asthma', 'Asthma']
    asthma_values = [asthma_peoples, len(document_ids) - asthma_peoples]

    asthma_f_l_u_name = []

    i = 0
    while (len(insert_doc_data) > i):
        if (insert_doc_data[i]['Asthma'] == 1):
            fname = insert_doc_data[i + 1]['FirstName']
            lname = insert_doc_data[i + 1]['LastName']
            uname = insert_doc_data[i + 1]['UserName']
            asthma_f_l_u_name.append(fname + ' ' + lname + '  -  ' + uname)
        i += 3

    return render_template('index.html',
                           set=zip(values, labels, colors), len=len(diabetes_f_l_u_name),Diabetes_List=diabetes_f_l_u_name,
                           chd_set=zip(chd_values, chd_labels, chd_colors),chd_len=len(chd_f_l_u_name), CHD_List=chd_f_l_u_name,
                           asthma_set=zip(asthma_values, asthma_labels, asthma_colors),asthma_len=len(asthma_f_l_u_name), asthma_List=asthma_f_l_u_name)

@app.route('/User_Details/<name>')
def User_Details(name):
    document_ids = []

    collections = fStore.collection("Users")
    for docs in collections.list_documents():
        document_ids.append(docs.id)

    insert_doc_data = []
    for ids in document_ids:
        collections = fStore.collection('Users').document(ids).collections()
        for collection in collections:
            for doc in collection.stream():
                temp = doc.to_dict()
                insert_doc_data.append(temp)

    i = 1

    while (len(insert_doc_data) >= i):
        if insert_doc_data[i]['UserName'] == name:
            # Personal Details
            Username = insert_doc_data[i]['UserName']
            fname = insert_doc_data[i]['FirstName']
            lname = insert_doc_data[i]['LastName']
            mob_no = insert_doc_data[i]['Mobile']
            email = insert_doc_data[i]['Email']

            # Watch Details
            body_temp = insert_doc_data[i + 1]['Body Temperature Count']
            calories = insert_doc_data[i + 1]['Calories Count']
            heart_rate = insert_doc_data[i + 1]['Heart Rate Count']
            oxygen_level = insert_doc_data[i + 1]['Oxygen Level Count']
            respiratory_rate = insert_doc_data[i + 1]['Respiratory Rate Count']
            step_count = insert_doc_data[i + 1]['Step Count']

            # Health Details
            age = insert_doc_data[i - 1]['Age']
            asthma = insert_doc_data[i - 1]['Asthma']
            bmi = insert_doc_data[i - 1]['BMI']
            chd = insert_doc_data[i - 1]['CHD']
            diabetes = insert_doc_data[i - 1]['Diabetes']
            gender = insert_doc_data[i - 1]['Gender']
            height = insert_doc_data[i - 1]['Height']
            med_records = insert_doc_data[i - 1]['Medical Records']
            stress_score = insert_doc_data[i - 1]['Stress Score']
            weight = insert_doc_data[i - 1]['Weight']

            if stress_score <= 6:
                stress_res = 'Low'
            elif stress_score >= 7 and stress_score <= 15:
                stress_res = 'Moderate'
            else:
                stress_res = 'High'

            break
        i += 3
    return render_template('user_details.html', DP=fname[0]+lname[0], UserName=name, FirstName=fname, LastName=lname, Email=email, Mobile_No=mob_no, body_temperature=body_temp, calories_count=calories, heart_rate_count=heart_rate, oxygen_level_count=oxygen_level, respiratory_rate_count=respiratory_rate, step_count=step_count, Age=age, Asthma=asthma, BMI=bmi, CHD=chd, Diabetes=diabetes, Gender=gender, Height=height, medical_records=med_records, stress_score=stress_res, Weight=weight)


if __name__ == '__main__':
    app.run(debug=True)
