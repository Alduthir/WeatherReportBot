# WeatherReportBot
A simple bot made to run in Heroku.
The bot uses `;` as it's prefix and communicates with the Google Places API, and the OpenWeather API.
## Commands
`;ping` used for testing

`;help` which PM's the user a help message.

`;locate <location>` which is used to return the latitude and longitude of the given place.

`;report <location> [units]` reports the current weather and temperature. Temperature in K by default, also accepts metric or imperial.
