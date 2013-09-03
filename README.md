Beskrivelse af LicenseModule-applikationen:
Applikationen kan styre adgang til resourcer ud fra WAYF attributter(eller ligende name/value par) ved at opbygge komplekse regler
ud fra disse.
Man opretter licenser ud fra WAYF attributer og definerer adgang til 
grupper samt præsentationstyper. En gruppe kan f.eks. være TV2 eller DR Radio mens en præsentationstype
kan være download eller image. Abstraktionen fra kanal og præsentations type kan generaliseres til en mængde af
elementer der kan være associseret til en anden mængde af elementer. (2-delt graf).

LicenseModule udstiller sine webservices via REST.
LicenseModule integererer til SOLR hvor man i gruppe-definitionerne skrives query-begræsninger. Dette er valgfrit.

=============
