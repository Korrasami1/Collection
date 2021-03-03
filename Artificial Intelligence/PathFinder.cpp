#include "PathFinder.h"
#include "Grid.h"
#include "GridTile.h"

void PathFinder::Create(Grid* gridmap, Player* player)
{
	this->gridmap = gridmap->GetComponent<GridBehaviourComponent*>();
	this->pacman = player;
}

void PathFinder::CalculateRoute(GridTile* start, GridTile* goal)
{
	this->start = start;
	this->goal = goal;

	PriorityQueue<GridTile*, double> frontier;			// Queue of tiles to check
	frontier.addElement(start, 0);
	
	came_from[start] = start;
	cost_so_far[start] = 0;

	while (!frontier.empty())
	{
		// Get a tile with the biggest priority
		GridTile* current = frontier.getElement();

		// If it's the goal, our path is complete
		if (current == goal)
			break;

		// Check all neighbours
		FindNeighbours(current, cost_so_far, goal, frontier, came_from);
	}
}

std::vector<GridTile*> PathFinder::FindPath()
{
	return FindPath(start, goal, came_from);
}

const std::vector<GridTile*> PathFinder::FindPath(GridTile* start, GridTile* goal, std::unordered_map<GridTile*, GridTile*>& came_from)
{
	std::vector<GridTile*> path;
	GridTile* current = goal;

	// Add all tiles of the path to a list
	while (current != NULL && current != start)
	{
		path.push_back(current);
		current = came_from[current];
	}

	path.push_back(start);
	// Reverse the list, so that we start at the beginning
	reverse(path.begin(), path.end());

	return path;
}

void PathFinder::FindNeighbours(GridTile*& current, std::unordered_map<GridTile*, double>& cost_so_far, GridTile* goal, PriorityQueue<GridTile*, double>& frontier, std::unordered_map<GridTile*, GridTile*>& came_from)
{
	std::vector<GridTile*> Neighbours = gridmap->GetNeighbours(current); // GetNeighbours checks the 4/8 grid tile positions around the current position and returns the neighbours which are not part of a wall/block tile
	for (auto next : Neighbours) {
		double new_cost = cost_so_far[current] + gridmap->Cost(current, next); //cost is based on the squared magnitude of the vector2d position of next

		if (cost_so_far.find(next) == cost_so_far.end() || new_cost < cost_so_far[next])
		{
			cost_so_far[next] = new_cost;
			double priority = new_cost + Heuristic(next, goal);
			frontier.addElement(next, priority);
			came_from[next] = current;
		}
	}
}

GridTile* PathFinder::GetGridTileLocation()
{
	return gridmap->GetGridTileLocation();
}

GridTile* PathFinder::GetGridTileLocation(int Type)
{
	return gridmap->GetGridTileLocation(Type);
}

GridTile* PathFinder::GetGridTileLocation(GameObject* object)
{
	return gridmap->GetGridTileLocation(object);
}

GridTile* PathFinder::GetPlayerPosition()
{
	return gridmap->GetGridTileLocation(pacman);
}

void PathFinder::Clear()
{
	came_from.clear();
	cost_so_far.clear();
	goal = nullptr;
	start = nullptr;
}

inline float PathFinder::Heuristic(GridTile* a, GridTile* b)
{
	return std::abs(a->GetPosition().x - b->GetPosition().x) + std::abs(a->GetPosition().y - b->GetPosition().y);
}
