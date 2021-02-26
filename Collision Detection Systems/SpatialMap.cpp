#include "spatial_map.h"
#include "component.h"

void SpatialMap::Create(int screenwidth, int screenheight, int radius)
{
    Cols = screenwidth / radius;
    Rows = screenheight / radius;
    ScreenWidth = screenwidth;
    ScreenHeight = screenheight;
    Radius = radius;
    CellSize = radius*2;
    
    Buckets = {};
    Clear();   
}

void SpatialMap::Init(GameObject* obj, ObjectPool<GameObject>* coll_objects)
{
    UpdateCollision(obj, coll_objects);
}

void SpatialMap::Clear()
{
    for (auto mapIterator = Buckets.begin(); mapIterator != Buckets.end(); mapIterator++) {
        mapIterator->second.clear();
    }

    Buckets.clear();
    for (int i = 0; i < Cols * Rows; i++)
    {
        std::vector<GameObject*> temp;
        Buckets[i] = temp;
    }
}

void SpatialMap::Register(GameObject* obj) //called in init of game
{
    std::vector<int> cellIds = GetObjectID(obj);

    for (auto item = cellIds.begin(); item != cellIds.end(); item++) 
    {
        Buckets[*item].push_back(obj);
    }
}

void SpatialMap::Add(Vector2D vector, float width, std::vector<int>* buckettoaddto)
{
    int cellPosition = (int)((std::floor(vector.x / CellSize)) + (std::floor(vector.y / CellSize)) * width);

    //Does not contain cellPosition then add to vector
    if (std::find(buckettoaddto->begin(), buckettoaddto->end(), cellPosition) != buckettoaddto->end()) 
    {
        return;
    }
    else {
        buckettoaddto->push_back(cellPosition);
    }
}

std::vector<int> SpatialMap::GetObjectID(GameObject* obj)
{
    std::vector<int> bucketsObjIsIn = std::vector<int>();

    Vector2D min = Vector2D(obj->position.x - (Radius), obj->position.y - (Radius));
    Vector2D max = Vector2D(obj->position.x + (Radius), obj->position.y + (Radius));

    float width = ScreenWidth / CellSize;

    //TopLeft
    Add(min, width, &bucketsObjIsIn);
    //TopRight
    Add(Vector2D(max.x, min.y), width, &bucketsObjIsIn);
    //BottomRight
    Add(Vector2D(max.x, max.y), width, &bucketsObjIsIn);
    //BottomLeft
    Add(Vector2D(min.x, max.y), width, &bucketsObjIsIn);

    return bucketsObjIsIn;
}

std::vector<GameObject*> SpatialMap::GetNearby(GameObject* obj) //called outside possibly in game
{
    std::vector<GameObject*> objects = std::vector<GameObject*>();
    std::vector<int> bucketIds = GetObjectID(obj);

    for (int i = 0; i < bucketIds.size(); i++)
    {
        objects = Buckets[bucketIds[i]]; //Adding with range
    }

    return objects;
}

void checkCollision(GameObject* ball, std::vector<GameObject*>& balls_to_check)
{
    CircleCollideComponent* circleCollider = ball->GetComponent<CircleCollideComponent*>();

    for (int i = 0; i < balls_to_check.size(); i++) {
        circleCollider->checkCollision(balls_to_check[i], ball);
    }
}

void SpatialMap::UpdateCollision(GameObject* obj, ObjectPool<GameObject>* coll_objects)
{
    //Clear buckets each update //clear each update because the objects move to they need to be reset in which buckets they belong
    Clear();

    //re-populate Buckets
    for (auto item = coll_objects->pool.begin(); item != coll_objects->pool.end(); item++) {
        Register(*item);
    }

    //Get all nearby objects to current gameobject
    std::vector<GameObject*> ObjectsToCheck = GetNearby(obj);

    //perform the collision check
    checkCollision(obj, ObjectsToCheck);
}

void SpatialMap::Destroy() //called in game
{
    Clear();
}
