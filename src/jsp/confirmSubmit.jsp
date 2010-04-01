<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
	import="org.ecocean.servlet.*,java.util.concurrent.ThreadPoolExecutor,java.util.Vector, java.io.FileReader, java.io.BufferedReader, java.util.Properties, java.util.Enumeration, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException,org.ecocean.*"%>
<%@ taglib uri="di" prefix="di"%>
<%
String number=request.getParameter("number");
Shepherd myShepherd=new Shepherd();

//setup our Properties object to hold all properties
	Properties props=new Properties();
	String langCode="en";
	Properties email_props=new Properties();
	
	//check what language is requested
	if(request.getParameter("langCode")!=null){
		if(request.getParameter("langCode").equals("fr")) {langCode="fr";}
		if(request.getParameter("langCode").equals("de")) {langCode="de";}
		if(request.getParameter("langCode").equals("es")) {langCode="es";}
	}
	
	//set up the file input stream
	props.load(getClass().getResourceAsStream("/bundles/"+langCode+"/submit.properties"));

	
	//FileInputStream propsInputStream2=new FileInputStream(new File((new File(".")).getCanonicalPath()+"/webapps/ROOT/WEB-INF/classes/bundles/en/confirmSubmitEmails.properties"));
	//email_props.load(propsInputStream2);
	email_props.load(getClass().getResourceAsStream("/bundles/"+langCode+"/confirmSubmitEmails.properties"));

	
	//load our variables for the submit page
	String title=props.getProperty("submit_title");
	String submit_maintext=props.getProperty("submit_maintext");
	String submit_reportit=props.getProperty("reportit");
	String submit_language=props.getProperty("language");
	String what_do=props.getProperty("what_do");
	String read_overview=props.getProperty("read_overview");
	String see_all_encounters=props.getProperty("see_all_encounters");
	String see_all_sharks=props.getProperty("see_all_sharks");
	String report_encounter=props.getProperty("report_encounter");
	String log_in=props.getProperty("log_in");
	String contact_us=props.getProperty("contact_us");
	String search=props.getProperty("search");
	String encounter=props.getProperty("encounter");
	String shark=props.getProperty("shark");
	String join_the_dots=props.getProperty("join_the_dots");
	String menu=props.getProperty("menu");
	String last_sightings=props.getProperty("last_sightings");
	String more=props.getProperty("more");
	String ws_info=props.getProperty("ws_info");
	String about=props.getProperty("about");
	String contributors=props.getProperty("contributors");
	String forum=props.getProperty("forum");
	String blog=props.getProperty("blog");
	String area=props.getProperty("area");
	String match=props.getProperty("match");
	
	//link path to submit page with appropriate language
	String submitPath="submit.jsp?langCode="+langCode;
	

%>

<html>
<head>
<title><%=CommonConfiguration.getHTMLTitle() %></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="Description"
	content="<%=CommonConfiguration.getHTMLDescription() %>" />
<meta name="Keywords"
	content="<%=CommonConfiguration.getHTMLKeywords() %>" />
<meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor() %>" />
<link href="<%=CommonConfiguration.getCSSURLLocation() %>"
	rel="stylesheet" type="text/css" />
<link rel="shortcut icon"
	href="<%=CommonConfiguration.getHTMLShortcutIcon() %>" />

</head>

<body>
<div id="wrapper">
<div id="page"><jsp:include page="header.jsp" flush="true">
	<jsp:param name="isResearcher"
		value="<%=request.isUserInRole("researcher")%>" />
	<jsp:param name="isManager"
		value="<%=request.isUserInRole("manager")%>" />
	<jsp:param name="isReviewer"
		value="<%=request.isUserInRole("reviewer")%>" />
	<jsp:param name="isAdmin" value="<%=request.isUserInRole("admin")%>" />
</jsp:include>
<div id="main">
<div id="leftcol">
<div id="menu">


<div class="module"><img src="images/area.jpg" width="190"
	height="115" border="0" title="Area to photograph"
	alt="Area to photograph" />
<p class="caption"><%=area%></p>
</div>

<div class="module"><img src="images/match.jpg" width="190"
	height="94" border="0" title="We Have A Match!" alt="We Have A Match!" />
<p class="caption"><%=match%></p>
</div>

</div>
<!-- end menu --></div>
<!-- end leftcol -->
<div id="maincol-wide">

<div id="maintext">
<%
		StringBuffer new_message=new StringBuffer();
		new_message.append("The ECOCEAN Library has received a new whale shark encounter submission. You can view it at:\nhttp://"+CommonConfiguration.getURLLocation()+"/encounters/encounter.jsp?number="+number);
		new_message.append("\n\nQuick stats:\n");
		String photographer="None";
		boolean emailPhoto=false;
		//get all needed DB reads out of the way in case Dynamic Image fails
		String addText="";
		boolean hasImages=true;
		String submitter="";
		String informMe="";
		if(!number.equals("fail")) {
		
			myShepherd.beginDBTransaction();
			try{
				Encounter enc=myShepherd.getEncounter(number);
				if((enc.getAdditionalImageNames()!=null)&&(enc.getAdditionalImageNames().size()>0)) {
					addText=(String)enc.getAdditionalImageNames().get(0);
				}
				if((enc.getLocationCode()!=null)&&(!enc.getLocationCode().equals("None"))) {
					informMe=email_props.getProperty(enc.getLocationCode());
				}
				else{hasImages=false;}
				new_message.append("Location: "+enc.getLocation()+"\n");
				new_message.append("Date: "+enc.getDate()+"\n");
				new_message.append("Size: "+enc.getSize()+" "+enc.getMeasureUnits()+"\n");
				new_message.append("Sex: "+enc.getSex()+"\n");
				new_message.append("Submitter: "+enc.getSubmitterName()+"\n");
				new_message.append("Email: "+enc.getSubmitterEmail()+"\n");
				new_message.append("Photographer: "+enc.getPhotographerName()+"\n");
				new_message.append("Email: "+enc.getPhotographerEmail()+"\n");
				new_message.append("Comments: "+enc.getComments()+"\n");
				submitter=enc.getSubmitterEmail();
				if((enc.getPhotographerEmail()!=null)&&(!enc.getPhotographerEmail().equals("None"))&&(!enc.getPhotographerEmail().equals(""))){
					photographer=enc.getPhotographerEmail();
					emailPhoto=true;
				}			
			}
			catch(Exception e) {System.out.println("Error encountered in confirmSubmit.jsp:");e.printStackTrace();}
			myShepherd.rollbackDBTransaction();	
			myShepherd.closeDBTransaction();
		}
		
		//addText="encounters/"+request.getParameter("number")+"/"+addText;
		String thumbLocation="file-encounters/"+number+"/thumb.jpg";
		if(myShepherd.isAcceptableVideoFile(addText)){addText="images/video_thumb.jpg";}
		else{addText="encounters/"+number+"/"+addText;}

			%> <di:img width="100" height="75" border="0" fillPaint="#000000"
	output="<%=thumbLocation%>" expAfter="0" threading="limited"
	align="left" valign="left">
	<di:image width="100" height="*" srcurl="<%=addText%>" />
</di:img>

<h1 class="intro">Success</h1>
<p><strong>Thank you for submitting your encounter! </strong></p>
<p>For future reference, this encounter has been assigned the number
<strong><%=number%></strong>.</p>
<p>If you have any questions, please reference this number when <a
	href="mailto:<%=CommonConfiguration.getAutoEmailAddress() %>">contacting
us.</a></p>

<p><a
	href="http://<%=CommonConfiguration.getURLLocation()%>/encounters/encounter.jsp?number=<%=number%>&langCode=<%=langCode%>">View
encounter #<%=number%></a>. <em>This may initially take a minute or
more to fully load as we dynamically copy-protect your new image(s).</em></p>
<%
		
		Vector e_images=new Vector();

		//get the email thread handler
		ThreadPoolExecutor es=MailThreadExecutorService.getExecutorService();


		//email the webmaster
		es.execute(new NotificationMailer(CommonConfiguration.getMailHost(), CommonConfiguration.getAutoEmailAddress(), CommonConfiguration.getNewSubmissionEmail(), ("New encounter submission: "+number), new_message.toString(), e_images));

		//now email those assigned this location code
		if(informMe!=null) {
			es.execute(new NotificationMailer(CommonConfiguration.getMailHost(), CommonConfiguration.getAutoEmailAddress(), informMe, ("New encounter submission: "+number), new_message.toString(), e_images));
		}

		//thank the submitter and photographer
		String thanksmessage=ServletUtilities.getText("thankyou.txt")+"\nEncounter :"+number+"\nhttp://"+CommonConfiguration.getURLLocation()+"/encounters/encounter.jsp?number="+number;
		es.execute(new NotificationMailer(CommonConfiguration.getMailHost(), CommonConfiguration.getAutoEmailAddress(), submitter, ("New encounter submission: "+number), thanksmessage, e_images));
		if(emailPhoto){

			es.execute(new NotificationMailer(CommonConfiguration.getMailHost(), CommonConfiguration.getAutoEmailAddress(), photographer, ("New encounter submission: "+number), thanksmessage, e_images));
		}
		%>
</div>
<!-- end maintext --></div>
<!-- end maincol --> <jsp:include page="footer.jsp" flush="true" /></div>
<!-- end page --></div>
<!--end wrapper -->
</body>
</html>