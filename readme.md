# Purpose

* ### Purpose takes a file(s) and customises it based on customisations defined in [devops.json](https://github.com/tinytelly/purpose/blob/master/devops.json).

### The benefit of using purpose is to reuse customisations and to write once, reuse often specific metadata often used in performing devops activities.  

# Purpose Rules
## 1) Any Purpose can reuse another existing purpose.  
To do this include the name of the other purpose in ``` devops.purpose ```

This example will define a purpose called ``` generic-database ``` which will apply everything defined in ``` generic-database-defaults ``` and set the value ``` deploy.database.name=V1_generic_db ``` in the file ``` devops.properties ```
```
          {
            "name": "generic-database",
            "files": {
              "files": [
                {
                  "name": "devops.properties",
                  "values": [
                    {
                      "name": "devops.purpose",
                      "value": "generic-database-defaults",
                      "state": "set"
                    },
                    {
                      "name": "deploy.database.name",
                      "value": "V1_generic_db",
                      "state": "overwrite"
                    }
                  ]
                }
              ]
            }
          }
```
Reusing purposes is very powerful and allows complex customisations to be boiled down into a single purpose.
The reused purpose tree can be seen in the logs and will be arranged like this example below for a complex purpose.

Each lower level in the purpose tree is represented by a ``` - ```  
As an example ``` ---> ``` would be a nesting that is 3 levels deep.
Which is to say that each purpose has reused a ``` devops.purpose ``` down to 3 levels ( lots of reuse of exsiting purposes....which is the point of the purpose framework.....to reuse customisations :-) 

```
NESTED PURPOSES :
 generic-1-0-deploy=AB-1234 
  -> generic-dev-database-1-0|disable-update-jira|deploy-branch-name=AB-1234|deploy-env-name=AB-1234
  --> generic-dev|disable-update-jira
  ---> generic-database-defaults|generic-stack|deploy-sub-domain=generic-devtest|generic-postcode-anywhere-development
  ----> generic-defaults|generic-internal
```


## 2) The order of applying purposes is bottom to top
This means the purpose set at the top of the nested tree will override anything set below it.

This example will apply  ``` acme ``` first then ``` acme-jboss ``` and finally ``` acme-app ```

Anything set in the first 2 acme purposes has the potential to be over written by ``` acme-app ```

The value in doing this is to be able to define a specific ``` acme-app ```  purpose while being able to inherit all the customisations for acme that happened in ``` acme ```  and ``` acme-jboss ``` .  Within ``` acme-app ``` you would only define things that are specific to an app version of acme.

Note: a | is used to separate purposes.

```
NESTED PURPOSES :
 acme-app
  -> acme-jboss
  --> acme|jboss
```

## 3) The order of applying reused purposes in devops.purpose is right to left
This example defines 2 reused purposes in ``` devops.purpose ``` which are ``` generic-database-defaults|generic ```

It will apply first ``` generic ``` then ``` generic-database-defaults ``` after that.

This allows you to ensure the run order of purposes where you can put any purpose that has precedence over another first in the list.
```
          {
            "name": "generic-database",
            "files": {
              "files": [
                {
                  "name": "devops.properties",
                  "values": [
                    {
                      "name": "devops.purpose",
                      "value": "generic-database-defaults|generic",
                      "state": "set"
                    }
                  ]
                }
              ]
            }
          }
```

## 4) Any value in devops.properties can be set using an Adhoc purpose
An adhoc purpose is one that is not predefined in ``` devops.json ```

If you wish to set ``` deploy.port=199 ``` then you can just provide and adhoc purpose of  ``` deploy-port=199 ```

Any new value you wish to set in ``` devops.properties ``` does not require a new entry in ``` devops.json ``` if you only intend to set a value to the property in ``` devops.properties ```

Note: a ``` - ``` used in a purpose, becomes a ``` . ``` in a properties file.

## 5) Files can share values
This example shows that the files ``` generic-api.properties|generic-authorisation.properties ``` will both add ``` id-number=1234 ```.

``` generic-api.properties ``` will also add ``` generic-api-id-number=5678 ``` but ``` generic-authorisation.properties ``` will not.
```
      {
        "name": "kubernetes-configmaps",
        "files": {
          "files": [
            {
              "name": "generic-api.properties|generic-authorisation.properties",
              "values": [
                {
                  "name": "id-number",
                  "value": "1234",
                  "state": "overwrite"
                }
              ]
            },
            {
              "name": "generic-api.properties",
              "values": [
                {
                  "name": "generic-api-id-number",
                  "value": "5678",
                  "state": "overwrite"
                }
              ]
            }
          ]
        }
      }
```


## Current list of Purposes
```
devops
   australia
   greeting
```
