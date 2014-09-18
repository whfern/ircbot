grammar IrcBot;

// Parser Rules

commandLine : cmd ;

// Lexer Rules

TEXT : ~[ ,\n\r"]+ ;
STRING : '"' ('""'|~'"')* '"' ; // quote-quote is an escaped quote
SPACE : ' ' ;

cmd : letMeGoogleThatForYou | google | stackOverflow | dictionary | duckDuckGo ;

letMeGoogleThatForYou : 'lmgtfy' SPACE arg ;

google : 'g' SPACE arg ;

duckDuckGo : 'duck' SPACE arg ;

stackOverflow : 'so' SPACE arg ;

dictionary : 'd' SPACE arg ;

arg : TEXT | STRING ;
