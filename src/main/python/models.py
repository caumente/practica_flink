import time
import pandas as pd
import numpy as np
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.ensemble import AdaBoostClassifier, VotingClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.model_selection import cross_val_score, KFold
from nyoka import skl_to_pmml
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn2pmml import PMMLPipeline, sklearn2pmml




def loadData(file_name, path_data = './../resources/'):
    data = pd.read_csv(path_data + file_name)

    return data

def testing_models():
    num_folds = 5
    kfold = KFold(n_splits=num_folds, shuffle=True)

    models = []
    models.append(("LDA", LinearDiscriminantAnalysis()))
    models.append(('Logistic regression', LogisticRegression()))
    models.append(('Ada Boost', AdaBoostClassifier()))
    models.append(("Gauss", GaussianNB()))
    models.append(("Voting", VotingClassifier(estimators=[
        ('LDA', LinearDiscriminantAnalysis()),
        ('LR', LogisticRegression()),
        ('AB', AdaBoostClassifier())], voting='soft')))


    start_time = time.time()
    # Evaluate each model in turn
    results = []
    names = []
    stds = []
    means = []
    for name, model in models:
        cv_results = cross_val_score(model,
                                     X_train.loc[:, X_train.columns != 'idx'],
                                     np.array(y_train.loc[:, y_train.columns != 'idx']).ravel(),
                                     cv=kfold,
                                     scoring='accuracy',
                                     n_jobs=2)
        print("Cross validation results for {0}: {1}".format(name, cv_results))
        print("{0}: ({1:.4f}) +/- ({2:.4f})".format(name, cv_results.mean(), cv_results.std()), "\n")
        results.append(cv_results)
        names.append(name)
        stds.append(cv_results.std())
        means.append(abs(cv_results.mean()))

    print("--- %s seconds ---" % (time.time() - start_time))

def model_to_pmml(X_train, y_train, result_key, algorithm_type, algorithm, out_path):
    """
    The Machine Learning model is exported to PMML format
    :param X_train: {array-like, sparse matrix}, shape (n_samples, n_features)
    Training DataFrame, where n_samples is the number of samples and n_features is the number of features.
    :param y_train: array-like, shape (n_samples,)
    Target DataFrame relative to X.
    :param result_key: Field of "y_field" that contains the result of the model
    :param algorithm_type: Type of algorithm applied in the model
    For example: 'classifier'
    :param algorithm: Instance of the algorithm applied in the model
    For example: estimator = LogisticRegression(random_state=random_state, solver=solver)
    .fit(X=X_train, y=y_train, sample_weight=sample_weight)
    :param out_path: Output file path in which the model in PMML format will be stored
    For example: "./Model.pmml"
    """
    # Create a PMMLPipeline object, and populate it with pipeline steps
    pipeline = PMMLPipeline([(algorithm_type, algorithm)])
    # The PMMLPipeline.fit (X, Y) method is invoked with both arguments to use their column names as feature names
    # (case of X) and destination names (case of y)
    pipeline.fit(X_train, y_train[result_key])
    # Convert the PMMLPipeline object to a PMML file in the local filesystem
    sklearn2pmml(pipeline, out_path, with_repr=True, debug=True)

"""
LOAD DATA
"""
X_train, y_train = loadData("X_train.csv"), loadData("y_train.csv")
X_test, y_test = loadData("X_test.csv"), loadData("y_test.csv")



"""
INFORMATION ABOUT DATA
"""
print("Train dataset contains {} rows and {} columns".format(X_train.shape[0], X_train.shape[1]))
print("Test dataset contains {} rows and {} columns \n".format(X_test.shape[0], X_test.shape[1]))


"""
TRAINING SOME MODELS TO DECIDE WHICH OF THEM IS THE BEST
"""
testing_models()




"""
STORE MODEL IN PMML FORMAT
"""

##########################
#   Logistic Regression  #
##########################
pipeline = Pipeline([
    ("model", LogisticRegression())
])
features = X_train.loc[:, X_train.columns != 'idx'].columns
pipeline.fit(X_train.loc[:, X_train.columns != 'idx'], y_train.loc[:, y_train.columns != 'idx'])

skl_to_pmml(pipeline=pipeline,
            col_names=features,
            target_name="labels",
            model_name="Classifier",
            pmml_f_name="./Model/logis_reg_model.pmml",
            description="This model has been implemented for a testing A/B in a Flink platform")

print("Model logistic regression stored successfully")


###########################
#   Gaussian Naïve Bayes  #
###########################
model_to_pmml(X_train=X_train.loc[:, X_train.columns != 'idx'],
              y_train=y_train.loc[:, y_train.columns != 'idx'],
              result_key='label',
              algorithm_type="classifier",
              algorithm=GaussianNB(),
              out_path="./Model/naive_bayes_model.pmml")

print("Model Gaussian Naïve Bayes stored successfully")

