
Created with something like
```bash
cat 997a.list | grep -v '\sKBD\W' | grep -v '\sDOD\W' | cut -d' ' -f1 | xargs -r -i bash -c "xmllint --xpath '/bib/record/datafield[@tag=\"595\"]/subfield[@code=\"a\"][contains(text(), \"99\")]/text()' {} 2>&1 || true" | sed 's/EMMSID//' | xargs -r -i echo 'https://soeg.kb.dk/discovery/fulldisplay?docid=alma{}&context=U&vid=45KBDK_KGL:KGL&lang=da'
```

* 12074100234E.txt EOD	KBU <https://soeg.kb.dk/discovery/fulldisplay?docid=alma994985737505761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 120760007908.txt EPT	KBU <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996466200105761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 120890001600.txt EPT	KBU <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898410805763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 121774018708.txt EPT	KBU <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123780657505763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 12184005880F.txt EPT	KBU 
* 121981614266.txt KBU    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123886388305763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130001858352.txt EJD	KBH <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995208665905761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130002251818.txt DOE	SKAF	UBF <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996484677805761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130002480371.txt EPT	KBU <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123829282305763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130002499291.txt KBU    
* 130004187947.txt UBF    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996466217205761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130004195151.txt KBJ    
* 130008546976.txt KBU    
* 130009693778.txt KBJ    
* 130014550462.txt EPT	UBF <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996484669505761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130014694650.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123805918305763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130018859611.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996465813705761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130019391125.txt EJD	KBH <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995208665905761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130020628015.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123793069705763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130020628023.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123793069605763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130020628074.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123793069805763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130020954281.txt SKAF	UBF <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996484678105761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130021245381.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996546107805761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130021434745.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995400994705761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130021434869.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995349240405761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130022436032.txt MUS    
* 130022506723.txt UBF    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995419070005761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130022777239.txt MUS    
* 130023078113.txt UBF    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123848728305763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023742854.txt KBU    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123886383305763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023744113.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996484776805761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023744423.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123809443805763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023744598.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996465914505761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745144.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123829380605763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745284.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123848724905763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745314.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123848724605763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745705.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123870295705763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745731.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123879502405763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745748.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123879502205763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745772.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123886402105763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023745901.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123886391105763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746086.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898417905763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746094.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898317005763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746221.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898271805763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746231.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898378305763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746396.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898363905763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746418.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898363605763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746531.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123905908705763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746574.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123910235805763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746604.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123910235505763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746620.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123915217305763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130023746639.txt MUS    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123915217205763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130024029906.txt UBF    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123806017405763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130024033172.txt EPT	KBU <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123886489205763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130024033962.txt UBF    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898294105763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130024035183.txt UBF    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898378505763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130024035248.txt UBF    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123898361005763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130024097677.txt EPT	KBU 
* 130024101542.txt EPT	UBF <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123905914505763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 130024102387.txt    <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123915217605763&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 20002343.txt 01	DNL	EPT 
* 400022152996.txt <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996484770205761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 400031348518.txt    
* 4135946732.txt  
* 4909375490.txt UBF  
* 5074279254.txt KBA	KBU	RUC	RUC02	UB1	UBF <https://soeg.kb.dk/discovery/fulldisplay?docid=alma995072152705761&context=U&vid=45KBDK_KGL:KGL&lang=da>
* 5157803532.txt KVINFO <https://soeg.kb.dk/discovery/fulldisplay?docid=alma996484780305761&context=U&vid=45KBDK_KGL:KGL&lang=da>   
* KB39670.txt EJD	KBH <https://soeg.kb.dk/discovery/fulldisplay?docid=alma99123793148605763&context=U&vid=45KBDK_KGL:KGL&lang=da>
