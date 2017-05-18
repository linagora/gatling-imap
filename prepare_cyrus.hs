#!/usr/bin/env stack
-- stack runghc --resolver lts-8.14 --package turtle --install-ghc
{-# LANGUAGE OverloadedStrings #-}

import Turtle hiding (append)
import Turtle.Format
import Data.Text(pack, append)
import Control.Monad
import Control.Monad.State as ST

csvLineToTuple = (\(user, _comma:password) -> (pack user, pack password)) . break (==',')

textToShellInput = select . textToLines

main = do
  shell "docker rm -f cyrus" empty
  shell "docker run -d --name cyrus -p 143:143 linagora/cyrus-imap" empty
  csvContent <- readFile "src/test/resources/data/users.csv"
  let usersAndPasswords = map csvLineToTuple $ drop 1 $ lines csvContent
  forM usersAndPasswords createUser
  shell "netcat localhost 143" $ textToShellInput $ constructIMapScript usersAndPasswords

createUser (user, password) =
  shell command empty
  where
    command = format ("docker exec -i cyrus bash -c 'echo "%s%" | saslpasswd2 -u test -c "%s%" -p'") password user

tagCommand :: Text ->  State Integer Text
tagCommand command = do
  x <- get
  put $ x + 1
  return $ format (d%" "%s) x command

constructIMapScript usersAndPasswords = evalState constructIMAP 0
  where
    lineAppend line1 line2 = line1 `append` "\n" `append` line2

    constructIMAP = do
      loginCommand <- tagCommand "LOGIN cyrus cyrus"
      creationCommands <- mapM createInboxs usersAndPasswords
      return $ foldl1 lineAppend $ loginCommand:creationCommands
  
    createInboxs (user, _) = do
      cmd1 <- tagCommand ("CREATE user." `append` user)
      cmd2 <- tagCommand $ (format ("CREATE user."%s%".INBOX") user)
      return $ cmd1 `lineAppend` cmd2
