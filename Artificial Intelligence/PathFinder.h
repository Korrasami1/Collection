#pragma once
#include <queue>
#include <unordered_map>
#include <functional>
#include "grid.h"
#include "player.h"
#include "GridTile.h"

//this pathfinder is used for the pacman game -> ghost a.i. paths

using namespace std;
class Ghost;

// Struct based on c# implementation
template<typename T, typename priority_t>
struct PriorityQueue {
	typedef std::pair<priority_t, T> PQElement;
	std::priority_queue<PQElement, std::vector<PQElement>,
		std::greater<PQElement >> elements;

	inline bool empty() const { return elements.empty(); }

	inline void addElement(T item, priority_t priority) {
		elements.emplace(priority, item);
	}

	inline T getElement() {
		T best_item = elements.top().second;
		elements.pop();
		return best_item;
	}
};

class PathFinder
{
	GridBehaviourComponent* gridmap;
	Player* pacman;

	std::unordered_map<GridTile*, GridTile*> came_from;		// Chosen path
	std::unordered_map<GridTile*, double> cost_so_far;	// Cost of movement
	GridTile* start;
	GridTile* goal;

public:

	~PathFinder() { SDL_Log("PathFinder::PathFinder"); };

	void Create(Grid* gridmap, Player* player);

	// Calculate a path for tile to tile
	void CalculateRoute(GridTile* start, GridTile* goal);
	std::vector<GridTile*> FindPath();

	const std::vector<GridTile*> FindPath(GridTile* goal, GridTile* start, std::unordered_map<GridTile*, GridTile*>& came_from);

	void FindNeighbours(GridTile*& current, std::unordered_map<GridTile*, double>& cost_so_far, GridTile* goal, PriorityQueue<GridTile*, double>& frontier, std::unordered_map<GridTile*, GridTile*>& came_from);

	//Find gridtiles to use for calculating routes - they inherit the grid functions to be able to use them seperately in game objects
	GridTile* GetGridTileLocation();
	GridTile* GetGridTileLocation(int Type);
	GridTile* GetGridTileLocation(GameObject* object);
	GridTile* GetPlayerPosition(); //special case
	void Clear();

private:
	static inline float Heuristic(GridTile* a, GridTile* b);
};

