//Inspired by https://conkerjo.wordpress.com/2009/06/13/spatial-hashing-implementation-for-fast-2d-collisions/
#pragma once
#include "game_object.h"
#include "object_pool.h"
#include <list>
#include <cmath>
#include <unordered_map>
#include <iostream>
#include <string>
#include <algorithm>

class SpatialMap
{
	friend class CircleCollideComponent;

	int Cols = 0;
	int Rows = 0;
	int ScreenWidth = 0;
	int ScreenHeight = 0;
	int CellSize = 0;
	int Radius = 0;

	std::unordered_map<int, std::vector<GameObject*>> Buckets;

public:
	void Create(int screenwidth, int screenheight, int radius);
	void Init(GameObject* obj, ObjectPool<GameObject>* coll_objects);
	void Clear();
	void Register(GameObject* obj);
	void Add(Vector2D vector, float width, std::vector<int>* buckettoaddto);
	std::vector<int> GetObjectID(GameObject* obj);
	std::vector<GameObject*> GetNearby(GameObject* obj);
	void UpdateCollision(GameObject* obj, ObjectPool<GameObject>* coll_objects);
	void Destroy();
};
