
# coding: utf-8

# In[1396]:


import pandas as pd 
import numpy as np
from scipy.stats import zscore


# In[1397]:


df = pd.read_csv('scores6.csv')


# In[1398]:


#df.drop('Judge', axis=1, inplace=True)


# In[1399]:


df.columns=["JudgeId","Team","p1","p2","p3","p4","Domain_preference"]


# In[1400]:


df.head()


# In[1401]:


n_judges = df['JudgeId'].max()
n_teams = df['Team'].max()
n_crit = 4
prefs_dict = {'1.0': 0.65, '2.0': 0.35, '3.0': 0.5}
print(n_judges, n_teams)


# In[1402]:


df['JudgeId']='j' + df['JudgeId'].astype(str)
df['Team']='t' + df['Team'].astype(str)
df.head()


# In[1403]:


df_pref=pd.pivot_table(df,values=['Domain_preference'], index='Team',columns=['JudgeId'])


# In[1404]:


df = pd.pivot_table(df, values = ['p1', 'p2', 'p3', 'p4'], index = 'Team', columns=['JudgeId'])


# In[1405]:


n_teams


# In[1406]:


df.head()


# In[1407]:


df.columns = ['_'.join(col).strip() for col in df.columns.values]


# In[1408]:


df.head()


# In[1409]:


for i in range(1, n_crit+1):
    for j in range(1, n_judges+1):
        try:
            if(df['p'+str(i)+'_j'+str(j)].std()==0.0):
                df['z'+str(i)+'_j'+str(j)]=(df['p'+str(i)+'_j'+str(j)].std())
            else:    
                df['z'+str(i)+'_j'+str(j)] = (df['p'+str(i)+'_j'+str(j)] - df['p'+str(i)+'_j'+str(j)].mean())/df['p'+str(i)+'_j'+str(j)].std()
        except KeyError:
            continue


# In[1410]:


df.head()


# In[1411]:


df['z1_j1'].head()


# In[1412]:


def calc_z(col, x):
    return (x - col.mean())/col.std()


# In[1413]:


norm_df = pd.DataFrame()


# In[1414]:


for i in range(1, n_crit+1):
    for j in range(1, n_judges+1):
        try:
            raw_max=(df['p'+str(i)+'_j'+str(j)].max())
            raw_min=(df['p'+str(i)+'_j'+str(j)].min())
            raw_col = df['p'+str(i)+'_j'+str(j)]
            z = df['z'+str(i)+'_j'+str(j)]
            z_max = z.max()
            z_min = z.min()
            norm_df['norm'+'_z'+str(i)+'_j'+str(j)] = 0.0
            norm_df['norm'+'_z'+str(i)+'_j'+str(j)] = norm_df['norm'+'_z'+str(i)+'_j'+str(j)].astype('float')
#             print((raw_max + raw_min)/2  + (raw_max - raw_min)/(z_max - z_min) * (z - (z_max + z_min)*0.5))
#             norm_df['norm'+'_z'+str(i)+'_j'+str(j)] = (raw_max + raw_min)/2.0  + (raw_max - raw_min)/(z_max - z_min) * (z - (z_max + z_min)/2)
            #print(raw_max, raw_min, z, z_max, z_min, calc_z(raw_col, raw_max + raw_min))
            norm_df['norm'+'_z'+str(i)+'_j'+str(j)] = (raw_max + raw_min)/2  + (raw_max - raw_min)/(z_max - z_min) * (z - calc_z(raw_col, raw_max + raw_min)*0.5)
        except KeyError:
            continue


# In[1415]:


df.head()


# In[1416]:


norm_df.head()


# In[1417]:


norm_df.norm_z1_j1.dtype


# ## norm_df.sum(axis='columns')

# In[1418]:


judge_wise_scores = pd.DataFrame()


# In[1419]:


for i in range(1, n_judges+1):
    judge_cols = norm_df.columns[norm_df.columns.str.contains('j' + str(i))].tolist()
    print(judge_cols)
    judge_wise_scores['j' + str(i)] = norm_df[judge_cols].sum(axis='columns')
    


# In[1420]:


judge_wise_scores


# In[1421]:


# judge_wise_scores['j2']=np.nan
# judge_wise_scores['j5']=np.nan


# In[1422]:


for i in range(1, n_teams+1 ):
    for j in range(1, n_judges+1):
        try:
            #print(df_pref['Domain_preference']['j'+str(j)]['t'+str(i)])
            df_pref['Domain_preference']['j'+str(j)]['t'+str(i)]=prefs_dict[str(df_pref['Domain_preference']['j'+str(j)]['t'+str(i)])]
        except KeyError:
            continue


# In[1423]:


df_pref


# In[1424]:


df_final=pd.DataFrame()


# In[1425]:


df_final=judge_wise_scores.copy()


# In[1426]:


df_final['j1']['t1'] = judge_wise_scores['j1']['t1']*df_pref['Domain_preference']['j1']['t1']


# In[1427]:


for i in range(1, n_teams+1 ):
    for j in range(1, n_judges+1):
        try:
            df_final['j'+str(j)]['t'+str(i)]=judge_wise_scores['j'+str(j)]['t'+str(i)]*df_pref['Domain_preference']['j'+str(j)]['t'+str(i)]
        except KeyError:
            continue


# In[1428]:


df_final.head()


# In[1429]:


df_final['agg_score'] = df_final.sum(axis='columns')


# In[1430]:


df_final=df_final.sort_values(by='agg_score', ascending=False)


# In[1431]:


df_final


# In[1432]:


df_final.to_csv('final_normalized_scores1.csv')

