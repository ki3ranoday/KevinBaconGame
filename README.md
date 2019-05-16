Made by Kieran O'Day Fall 2018 as an assignment for COSC 10 at Dartmouth College
# KevinBaconGame
In the Kevin Bacon game, the vertices are actors and the edge relationship is "appeared together in a movie". The goal is to find the shortest path between two actors. Traditionally the goal is to find the shortest path to Kevin Bacon, but we'll allow anybody to be the center of the acting universe.
## Commands
Different Commands will allow you to control the game:
c <#>:          list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation
d <low> <high>: list actors sorted by degree, with degree between low and high
i:              list actors with infinite separation from the current center
p <name>:       find path from <name> to current center of the universe
s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high
u <name>:       make <name> the center of the universe
q:              quit game
h:              help
