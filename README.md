## Overview
An application that allows users to batch download and update one or more Pixiv series for those who want to keep an offline library of them. 
The application will save a database of any series you want to keep track of, which then you can easily batch download and/or update them.

## Quick Tutorial
1. Upon first opening the app, input the configuration settings which include the download location, file format, and file naming format
2. Use the Add Series button to add a series to the database
3. Use the Download button to download all the available chapters of all series in the database to the download location

## .jar File Location
The .jar file can be placed anywhere but upon initialization it will create a the database file "PixivSeriesDownloader.db" and the config file "app.config" in the same directory as the 
.jar file. Keep these in mind, if you want to move the .jar file to somewhere else these 2 other files will also need to be moved. It's also not recommended to modify the contents of these 
files except through the app itself.

## Config 
<img width="491" height="342" alt="{DEE7AFF4-9FC3-4B3F-9350-E02AA55A90BE}" src="https://github.com/user-attachments/assets/b6dcadad-6a78-45af-9e8c-9053fdc28146" />

The config allows the modification of the downloader properties. It is prompted upon first opening the app and can also be accessed later on.
1. Library Location is where all the files will be downloaded to.
2. Save Series As determines the file format of the downloaded chapters. The available options are .zip, .cbz, .pdf, and folder which simply store the images in an uncompressed folder.
3. The chapter naming format determines the naming convention of the downloaded chapters. Note that either {chapter_id} or {chapter_number} is mandatory to include to prevent overlapping chapter names.

## Main Menu
<img width="294" height="320" alt="{96F6214F-10D2-4350-8E7B-0ED7BE686E8C}" src="https://github.com/user-attachments/assets/c779741f-715f-475b-b163-2c80e10d5ec7" />

The main menu is the hub where all the other functions are accessed through.

## Download Menu
<img width="596" height="425" alt="{0CB77587-E19F-4E49-8299-8300C3A04C4E}" src="https://github.com/user-attachments/assets/9cb307cc-b934-4b84-8021-d9580b47f614" />

The download menu is where you can make the app download the chapters. Note that the downloader will only download series marked as ONGOING (more on that on the Database View Menu below).
1. Download All from Latest Chapter: Download only the ones that haven't been downloaded (or skipped) by the downloader before.
2. Redownload All from the First Chapter: Forces the downloader to download all the chapters, even ones that has been downloaded before.
3. If the downloader encounters an error, it will prompt the user for what action to take.

## Login Menu
<img width="291" height="278" alt="{126C1069-86A8-4C07-95BE-0B50EC3ED12B}" src="https://github.com/user-attachments/assets/38e7437e-4b1d-4f15-8ab9-50179452fc0d" />

The login menu allows the user to login to Pixiv, which is needed when trying to download a chapter containing sensitive (18+) content.
1. The login session cannot be remembered and the user must login every time they open the app
2. Save credentials can save the username and password for easier login next time
3. The login button will attempt to login with the given credentials.
4. Login manually will open the browser and show it to the user for them to login manually. This is mainly used if the login button fails due to reCaptcha (which the user will be alerted to). Note that due to technical limitations, the browser must be kept open for the remainder of the session (but it can be minimized).

## Add Series Menu
<img width="487" height="122" alt="{27BF36FE-4B5D-4EB9-8B1D-BC36B1206858}" src="https://github.com/user-attachments/assets/2825f10a-56ea-4c9d-b07f-653fef08da97" />

The add series menu allows users to add a series to the database. Simply add the series URL (the one you access by clicking "table of contents" in a chapter page) and click the parse button to 
let the app fetch details about the series.

<img width="490" height="584" alt="{5004B678-06BE-4DF2-BDC2-1F98CB1D7993}" src="https://github.com/user-attachments/assets/80680fee-3966-4a68-a1a9-f0cbe3e0ead0" />

Here is an example of what it looks like after the parsing is complete.
1. The file directories of the download location is formatted as Library\Group\SeriesName
2. The group directory determines the "group" folder the series will be downloaded at. How will they be grouped is up to you. You can make custom tags like "comedy" and "action", or you can group them based on the artist name, or you can even not use any group directory and simply place the series directly in the download location root.
3. Title directory determines the "series" folder that the series will be downloaded at. By default this is simply the series' title.
4. The artist, title, and preview image is there for the user to confirm that they've parsed the correct series and didn't make a typo in the url.
5. The directory view shows how the download location directories will be like
6. Save series to the database once everything is good

## Database View Menu
<img width="598" height="439" alt="{3EC62669-0AEF-484D-9D82-C79E731CDD4E}" src="https://github.com/user-attachments/assets/14af9fc4-a0cf-4ad6-ad74-e53641fc19e0" />

The database view menu allows the viewing and editing of any series that has already been saved into the database. Click on any of the series to view or edit them.
1. Change series status allows the changing of their status. The options are ONGOING, PAUSED, HIATUS, COMPLETED, DELETED. The downloader will only download series with the ONGOING status, changing their status into anything else will cause them to get skipped by the downloader. Note that the downloader also can detect if a series is deleted and automatically change their status in that case.
<img width="353" height="269" alt="{57A002CC-19BE-49DA-87D4-CDFF7EA98326}" src="https://github.com/user-attachments/assets/24f89948-db15-4eb5-a25a-5081c29103eb" />

2. Copy link to clipboard and open in browser allows the user to access the series directly on their own browser if needed.
3. Delete series will remove the series from the database.
4. The database view will use a tableview by default, but a treeview is also available where the series are depicted as how they are in the download location.
<img width="595" height="434" alt="{F1679CBC-DE11-466E-9485-6D6204C2257B}" src="https://github.com/user-attachments/assets/a829777f-4919-4490-a6fa-6c077117edaf" />
